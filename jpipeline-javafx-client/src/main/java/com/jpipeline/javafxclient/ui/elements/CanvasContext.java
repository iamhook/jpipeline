package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.dto.NodeDTO;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;

import java.util.HashMap;
import java.util.Map;

public class CanvasContext {

    private static final int NODE_HEIGHT = 50;
    private static final int NODE_WIDTH = 100;
    private static final double DEFAULT_X = 200;
    private static final double DEFAULT_Y = 200;

    private Pane rootPane;
    private Canvas canvas;

    private final double canvasHeight;
    private final double canvasWidth;

    private Map<NodeDTO, NodeWrapper> nodeWrappers = new HashMap<>();

    private NodeDTO connectingNode;
    private ConnectionType currentConnectionType;

    public CanvasContext(Pane rootPane) {
        this.rootPane = rootPane;

        canvasHeight = rootPane.getPrefHeight();
        canvasWidth = rootPane.getPrefWidth();

        this.canvas = new Canvas(canvasWidth, canvasHeight);
        rootPane.getChildren().add(canvas);
        setClip();
        createCanvasGrid(true);
    }

    private void setClip() {
        Rectangle clip = new Rectangle(canvasWidth, canvasHeight);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        rootPane.setClip(clip);
    }

    private void createCanvasGrid(boolean sharp) {
        GraphicsContext gc = canvas.getGraphicsContext2D() ;
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
        for (int x = 0; x < canvas.getWidth(); x+=10) {
            double x1 ;
            if (sharp) {
                x1 = x + 0.5 ;
            } else {
                x1 = x ;
            }
            gc.moveTo(x1, 0);
            gc.lineTo(x1, canvas.getHeight());
            gc.stroke();
        }

        for (int y = 0; y < canvas.getHeight(); y+=10) {
            double y1 ;
            if (sharp) {
                y1 = y + 0.5 ;
            } else {
                y1 = y ;
            }
            gc.moveTo(0, y1);
            gc.lineTo(canvas.getWidth(), y1);
            gc.stroke();
        }
    }

    public void connectNodes(NodeDTO fromNode, NodeDTO toNode) {
        NodeWrapper fromNodeWrapper = nodeWrappers.get(fromNode);
        NodeWrapper toNodeWrapper = nodeWrappers.get(toNode);
        Circle fromHandle = fromNodeWrapper.getOutputHandle();
        Circle toHandle = toNodeWrapper.getInputHandle();

        MoveTo from = new MoveTo(fromHandle.getCenterX(), fromHandle.getCenterY());
        LineTo to = new LineTo(toHandle.getCenterX(), toHandle.getCenterY());

        Path path = new Path(from, to);
        path.setStrokeWidth(3);

        fromNodeWrapper.addOutput(path);
        toNodeWrapper.addInput(path);

        fromNode.addWire(toNode.getId());

        rootPane.getChildren().add(path);

        connectingNode = null;
        currentConnectionType = null;
    }


    public Rectangle createNodeRectangle(NodeDTO node) {
        if (node.getX() == null)
            node.setX(DEFAULT_X);
        if (node.getY() == null)
            node.setY(DEFAULT_Y);

        Rectangle rect = new Rectangle(node.getX(), node.getY(), NODE_WIDTH, NODE_HEIGHT);

        NodeWrapper nodeWrapper = new NodeWrapper();
        nodeWrapper.setRectangle(rect);

        rect.setFill(Paint.valueOf(node.getColor()));

        double radius = 10.0f;


        Circle outputHandle = new Circle(radius, Color.RED);
        outputHandle.centerXProperty().bind(rect.xProperty().add(rect.widthProperty()));
        outputHandle.centerYProperty().bind(rect.yProperty().add(rect.heightProperty().divide(2)));

        Circle inputHandle = new Circle(radius, Color.RED);
        inputHandle.centerXProperty().bind(rect.xProperty().add(rect.getTranslateX()));
        inputHandle.centerYProperty().bind(rect.yProperty().add(rect.heightProperty().divide(2)));

        nodeWrapper.setInputHandle(inputHandle);
        nodeWrapper.setOutputHandle(outputHandle);

        rect.parentProperty().addListener((obs, oldParent, newParent) -> {
            inputHandle.setOnMouseClicked(event -> {
                if (connectingNode == null) {
                    currentConnectionType = ConnectionType.INPUT_TO_OUTPUT;
                    connectingNode = node;
                } else if (currentConnectionType.equals(ConnectionType.OUTPUT_TO_INPUT)) {
                    connectNodes(connectingNode, node);
                }
            });
            outputHandle.setOnMouseClicked(event -> {
                if (connectingNode == null) {
                    currentConnectionType = ConnectionType.OUTPUT_TO_INPUT;
                    connectingNode = node;
                } else if (currentConnectionType.equals(ConnectionType.INPUT_TO_OUTPUT)) {
                    connectNodes(node, connectingNode);
                }
            });
            outputHandle.setOnMouseEntered(event -> {
                outputHandle.setFill(Color.BLACK);
            });
            outputHandle.setOnMouseExited(event -> {
                outputHandle.setFill(Color.RED);
            });
            Pane currentParent = (Pane)outputHandle.getParent();
            if (currentParent != null) {
                currentParent.getChildren().remove(outputHandle);
            }
            ((Pane)newParent).getChildren().add(outputHandle);
            ((Pane)newParent).getChildren().add(inputHandle);

        });

        Wrapper<Point2D> mouseLocation = new Wrapper<>();

        //setUpDragging(moveHandle, mouseLocation);
        setUpDragging(rect, mouseLocation);

        rect.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX ;
                double newY = rect.getY() + deltaY ;
                rect.setX(newX);
                rect.setY(newY);
                node.setX(newX);
                node.setY(newY);
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());

                for (Path path : nodeWrapper.getInputs()) {
                    Circle input = nodeWrapper.getInputHandle();
                    LineTo lineTo = (LineTo) path.getElements().get(1);
                    lineTo.setX(input.getCenterX());
                    lineTo.setY(input.getCenterY());
                }

                for (Path path : nodeWrapper.getOutputs()) {
                    Circle input = nodeWrapper.getOutputHandle();
                    MoveTo moveTo = (MoveTo) path.getElements().get(0);
                    moveTo.setX(input.getCenterX());
                    moveTo.setY(input.getCenterY());
                }
            }
        });

        rootPane.getChildren().add(rect);

        nodeWrappers.put(node, nodeWrapper);
        return rect ;
    }

    private static void setUpDragging(Shape shape, Wrapper<Point2D> mouseLocation) {

        shape.setOnDragDetected(event -> {
            shape.getParent().setCursor(Cursor.CLOSED_HAND);
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        shape.setOnMouseReleased(event -> {
            shape.getParent().setCursor(Cursor.DEFAULT);
            mouseLocation.value = null ;
        });
    }

    static class Wrapper<T> { T value ; }

    private enum ConnectionType {
        OUTPUT_TO_INPUT,
        INPUT_TO_OUTPUT
    }
}
