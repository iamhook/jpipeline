package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.ui.util.InterfaceHelper;
import com.jpipeline.javafxclient.ui.util.ViewHelper;
import com.jpipeline.javafxclient.ui.util.Wrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jpipeline.javafxclient.Consts.*;

public class ViewWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ViewWorkflowService.class);

    private Pane rootPane;

    private final double canvasHeight = 2000;
    private final double canvasWidth = 2000;

    private Map<NodeDTO, NodeWrapper> nodeWrappers = new HashMap<>();

    private NodeDTO connectingNode;
    private CubicCurve connectingWire;
    private ConnectionType currentConnectionType;

    private WorkflowService workflowService;

    public ViewWorkflowService(Pane rootPane, WorkflowService workflowService) {
        this.rootPane = rootPane;
        this.workflowService = workflowService;

        rootPane.setPrefHeight(canvasHeight);
        rootPane.setPrefWidth(canvasWidth);
        setClip();

        rootPane.setOnMouseMoved(event -> {
            if (connectingWire != null) {
                if (currentConnectionType.equals(ConnectionType.INPUT_TO_OUTPUT)) {
                    updateCurve(event.getX(), event.getY(), connectingWire.getEndX(), connectingWire.getEndY(), connectingWire);
                } else {
                    updateCurve(connectingWire.getStartX(), connectingWire.getStartY(), event.getX(), event.getY(), connectingWire);
                }
                sortChildren();
            }
        });

        rootPane.setOnMouseClicked(event -> {
            if (connectingWire != null) {
                rootPane.getChildren().remove(connectingWire);
                connectingWire = null;
                connectingNode = null;
                currentConnectionType = null;
            }
        });
    }

    private void setClip() {
        Rectangle clip = new Rectangle(canvasWidth, canvasHeight);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        rootPane.setClip(clip);
    }

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

    public void deleteNode(NodeDTO node) {
        NodeWrapper nodeWrapper = nodeWrappers.get(node);
        nodeWrapper.destroy();
        nodeWrappers.remove(node);
    }

    public void connectNodes(NodeDTO fromNode, NodeDTO toNode) {
        NodeWrapper fromNodeWrapper = nodeWrappers.get(fromNode);
        NodeWrapper toNodeWrapper = nodeWrappers.get(toNode);
        Circle fromHandle = fromNodeWrapper.getOutputHandle();
        Circle toHandle = toNodeWrapper.getInputHandle();

        double fromX = fromHandle.getCenterX();
        double fromY = fromHandle.getCenterY();
        double toX = toHandle.getCenterX();
        double toY = toHandle.getCenterY();

        CubicCurve curve = ViewHelper.createConnectionCurve();

        updateCurve(fromX, fromY, toX, toY, curve);

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

    private void updateCurve(double fromX, double fromY, double toX,
                             double toY, CubicCurve curve) {
        double yControlOffset = toY > fromY ? NODE_HEIGHT / 2f: NODE_HEIGHT / -2f;
        double xControlOffset = NODE_WIDTH * 1.5;

        double sqrt = Math.sqrt(Math.pow(Math.abs(toX - fromX), 2) + Math.pow(Math.abs(toY - fromY), 2));
        xControlOffset = Math.min(sqrt, xControlOffset);

        curve.setStartX(fromX);
        curve.setStartY(fromY);
        curve.setControlX1(fromX + xControlOffset);
        //curve.setControlY1((fromY + toY) / 2);
        curve.setControlY1(fromY + yControlOffset);
        curve.setControlX2(toX - xControlOffset);
        //curve.setControlY2((fromY + toY) / 2);
        curve.setControlY2(toY - yControlOffset);
        curve.setEndX(toX);
        curve.setEndY(toY);
    }

    public void createNode(NodeDTO node, boolean deployed) {

        if (node.getX() == null)
            node.setX(DEFAULT_X);
        if (node.getY() == null)
            node.setY(DEFAULT_Y);

        Rectangle rectangle = ViewHelper.createNodeRectangle(Paint.valueOf(node.getColor()));

        if (!deployed) {
            rectangle.setOpacity(0.7);
        }
        rectangle.setWidth(NODE_WIDTH);
        rectangle.setHeight(NODE_HEIGHT);
        rectangle.setX(node.getX());
        rectangle.setY(node.getY());
        rootPane.getChildren().add(rectangle);

        NodeWrapper nodeWrapper = new NodeWrapper(node);
        nodeWrapper.setParent(rootPane);
        nodeWrapper.setRectangle(rectangle);

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
                connectingWire = ViewHelper.createConnectionCurve();
                updateCurve(inputHandle.getCenterX(), inputHandle.getCenterY(), inputHandle.getCenterX(), inputHandle.getCenterY(), connectingWire);
                rootPane.getChildren().add(connectingWire);
            } else if (currentConnectionType.equals(ConnectionType.OUTPUT_TO_INPUT)) {
                workflowService.connectNodes(connectingNode, node);
                rootPane.getChildren().remove(connectingWire);
                connectingWire = null;
            }
        });
        outputHandle.setOnMouseClicked(event -> {
            if (connectingNode == null) {
                currentConnectionType = ConnectionType.OUTPUT_TO_INPUT;
                connectingNode = node;
                connectingWire = ViewHelper.createConnectionCurve();
                updateCurve(outputHandle.getCenterX(), outputHandle.getCenterY(), outputHandle.getCenterX(), outputHandle.getCenterY(), connectingWire);
                rootPane.getChildren().add(connectingWire);
            } else if (currentConnectionType.equals(ConnectionType.INPUT_TO_OUTPUT)) {
                workflowService.connectNodes(node, connectingNode);
                rootPane.getChildren().remove(connectingWire);
                connectingWire = null;
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

                    updateCurve(fromX, fromY, toX, toY, curve);
                }
                for (CubicCurve curve : nodeWrapper.getOutputs()) {
                    Circle from = nodeWrapper.getOutputHandle();
                    double fromX = from.getCenterX();
                    double fromY = from.getCenterY();
                    double toX = curve.getEndX();
                    double toY = curve.getEndY();

                    updateCurve(fromX, fromY, toX, toY, curve);
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

    public void destroy() {
        nodeWrappers.values().forEach(NodeWrapper::destroy);
        nodeWrappers.clear();
    }

    private enum ConnectionType {
        OUTPUT_TO_INPUT,
        INPUT_TO_OUTPUT
    }

    private static final List<Class<? extends Node>> elementsOrder = Arrays.asList(
            Rectangle.class,
            CubicCurve.class,
            Path.class,
            Circle.class,
            Text.class
    );
}
