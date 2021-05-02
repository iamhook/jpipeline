package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.ui.util.InterfaceHelper;
import com.jpipeline.javafxclient.ui.util.ViewHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewWorkflowService implements IWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ViewWorkflowService.class);

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

    private WorkflowService workflowService;

    public ViewWorkflowService(Pane rootPane, WorkflowService workflowService) {
        this.rootPane = rootPane;
        this.workflowService = workflowService;

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

    @Override
    public void disconnectNodes(NodeDTO fromNode, NodeDTO toNode) {
        NodeWrapper fromNodeWrapper = nodeWrappers.get(fromNode);
        NodeWrapper toNodeWrapper = nodeWrappers.get(toNode);

        CubicCurve curve = fromNodeWrapper.getOutputs().stream()
                .filter(p -> toNodeWrapper.getInputs().contains(p))
                .findFirst().orElse(null);

        fromNodeWrapper.getOutputs().remove(curve);
        toNodeWrapper.getInputs().remove(curve);
        rootPane.getChildren().remove(curve);
    }

    @Override
    public void deleteNode(NodeDTO node) {
        NodeWrapper nodeWrapper = nodeWrappers.get(node);
        nodeWrapper.destroy();
        nodeWrappers.remove(node);
    }

    @Override
    public void connectNodes(NodeDTO fromNode, NodeDTO toNode) {
        NodeWrapper fromNodeWrapper = nodeWrappers.get(fromNode);
        NodeWrapper toNodeWrapper = nodeWrappers.get(toNode);
        Circle fromHandle = fromNodeWrapper.getOutputHandle();
        Circle toHandle = toNodeWrapper.getInputHandle();

        double fromX = fromHandle.getCenterX();
        double fromY = fromHandle.getCenterY();
        double toX = toHandle.getCenterX();
        double toY = toHandle.getCenterY();

        CubicCurve curve = new CubicCurve(fromX, fromY,
                fromX + 200, (fromY + toY) / 2,
                toX - 200, (fromY + toY) / 2,
                toX, toY);

        curve.setStroke(Color.FORESTGREEN);
        curve.setStrokeWidth(3);
        curve.setStrokeLineCap(StrokeLineCap.ROUND);
        curve.setFill(Color.TRANSPARENT);

        curve.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                workflowService.disconnectNodes(fromNode, toNode);
            }
        });

        fromNodeWrapper.addOutput(curve);
        toNodeWrapper.addInput(curve);

        rootPane.getChildren().add(curve);

        sortChildren();

        connectingNode = null;
        currentConnectionType = null;
    }

    @Override
    public void createNode(NodeDTO node) {

        if (node.getX() == null)
            node.setX(DEFAULT_X);
        if (node.getY() == null)
            node.setY(DEFAULT_Y);

        Rectangle rectangle = new Rectangle(node.getX(), node.getY(), NODE_WIDTH, NODE_HEIGHT);
        rootPane.getChildren().add(rectangle);

        NodeWrapper nodeWrapper = new NodeWrapper(node);
        nodeWrapper.setParent(rootPane);
        nodeWrapper.setRectangle(rectangle);

        rectangle.setStroke(Color.BLACK);
        rectangle.setFill(Paint.valueOf(node.getColor()));

        Circle outputHandle = ViewHelper.createOutputHandle(rectangle);
        Circle inputHandle = ViewHelper.createInputHandle(rectangle);
        Circle closeHandle = ViewHelper.createCloseHandle(rectangle);

        Text nameLabel = ViewHelper.createNameLabel(node.getType(), rectangle);
        Text statusLabel = ViewHelper.createStatusLabel(rectangle);
        rootPane.getChildren().add(outputHandle);
        rootPane.getChildren().add(inputHandle);
        rootPane.getChildren().add(closeHandle);
        rootPane.getChildren().add(nameLabel);
        rootPane.getChildren().add(statusLabel);

        nodeWrapper.setInputHandle(inputHandle);
        nodeWrapper.setOutputHandle(outputHandle);
        nodeWrapper.setCloseHandle(closeHandle);
        nodeWrapper.setNameLabel(nameLabel);
        nodeWrapper.setStatusLabel(statusLabel);

        inputHandle.setOnMouseClicked(event -> {
            if (connectingNode == null) {
                currentConnectionType = ConnectionType.INPUT_TO_OUTPUT;
                connectingNode = node;
            } else if (currentConnectionType.equals(ConnectionType.OUTPUT_TO_INPUT)) {
                workflowService.connectNodes(connectingNode, node);
            }
        });
        outputHandle.setOnMouseClicked(event -> {
            if (connectingNode == null) {
                currentConnectionType = ConnectionType.OUTPUT_TO_INPUT;
                connectingNode = node;
            } else if (currentConnectionType.equals(ConnectionType.INPUT_TO_OUTPUT)) {
                workflowService.connectNodes(node, connectingNode);
            }
        });
        closeHandle.setOnMouseClicked(event -> {
            workflowService.deleteNode(node);
        });

        Wrapper<Point2D> mouseLocation = new Wrapper<>();
        setUpDragging(rectangle, mouseLocation);
        rectangle.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rectangle.getX() + deltaX ;
                double newY = rectangle.getY() + deltaY ;
                rectangle.setX(newX);
                rectangle.setY(newY);
                node.setX(newX);
                node.setY(newY);
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());

                for (CubicCurve curve : nodeWrapper.getInputs()) {
                    Circle to = nodeWrapper.getInputHandle();
                    double fromX = curve.getStartX();
                    double fromY = curve.getStartY();
                    double toX = to.getCenterX();
                    double toY = to.getCenterY();

                    curve.setEndX(toX);
                    curve.setEndY(toY);

                    curve.setControlX1(fromX + 200);
                    curve.setControlY1((fromY + toY) / 2);
                    curve.setControlX2(toX - 200);
                    curve.setControlY2((fromY + toY) / 2);
                }
                for (CubicCurve curve : nodeWrapper.getOutputs()) {
                    Circle from = nodeWrapper.getOutputHandle();
                    double fromX = from.getCenterX();
                    double fromY = from.getCenterY();
                    double toX = curve.getEndX();
                    double toY = curve.getEndY();

                    curve.setStartX(fromX);
                    curve.setStartY(fromY);

                    curve.setControlX1(fromX + 200);
                    curve.setControlY1((fromY + toY) / 2);
                    curve.setControlX2(toX - 200);
                    curve.setControlY2((fromY + toY) / 2);
                }
            }
        });

        rectangle.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                InterfaceHelper.showNodeEditMenu(nodeWrapper, ((Node)event.getSource()).getScene().getWindow());
            }
        });


        nodeWrappers.put(node, nodeWrapper);

        nodeWrapper.init();
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

    private void sortChildren() {
        ObservableList<Node> workingCollection = FXCollections.observableArrayList(rootPane.getChildren());
        workingCollection.sort((o1, o2) -> {
            int i1 = elementsOrder.indexOf(o1.getClass());
            int i2 = elementsOrder.indexOf(o2.getClass());
            return Integer.compare(i1, i2);
        });
        rootPane.getChildren().setAll(workingCollection);
    }

    static class Wrapper<T> { T value ; }

    private enum ConnectionType {
        OUTPUT_TO_INPUT,
        INPUT_TO_OUTPUT
    }

    private static final List<Class<? extends Node>> elementsOrder = Arrays.asList(
            Canvas.class,
            Rectangle.class,
            CubicCurve.class,
            Path.class,
            Circle.class,
            Text.class
    );
}
