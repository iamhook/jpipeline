package com.jpipeline.javafxclient.ui;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.util.InterfaceHelper;
import com.jpipeline.javafxclient.ui.util.CanvasHelper;
import com.jpipeline.javafxclient.ui.util.Wrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

        CubicCurve curve = CanvasHelper.createConnectionCurve();

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
        curve.setControlY1(fromY + yControlOffset);
        curve.setControlX2(toX - xControlOffset);
        curve.setControlY2(toY - yControlOffset);
        curve.setEndX(toX);
        curve.setEndY(toY);
    }

    public void createNode(NodeDTO node, boolean deployed) {

        NodeConfig nodeConfig = NodeService.getNodeConfig(node.getType());

        if (node.getX() == null)
            node.setX(DEFAULT_X);
        if (node.getY() == null)
            node.setY(DEFAULT_Y);

        Rectangle rectangle = CanvasHelper.createNodeRectangle(Paint.valueOf(node.getColor()));

        rectangle.setWidth(NODE_WIDTH);
        rectangle.setHeight(NODE_HEIGHT);
        rectangle.setX(node.getX());
        rectangle.setY(node.getY());
        rootPane.getChildren().add(rectangle);

        NodeWrapper nodeWrapper = new NodeWrapper(node);
        nodeWrapper.setParent(rootPane);
        nodeWrapper.setRectangle(rectangle);

        Circle closeHandle = CanvasHelper.createCloseHandle(rectangle);

        Text nameLabel = CanvasHelper.createNameLabel(node.getType(), rectangle);
        Text statusLabel = CanvasHelper.createStatusLabel(rectangle);

        rootPane.getChildren().addAll(closeHandle, nameLabel, statusLabel);

        nodeWrapper.setCloseHandle(closeHandle);
        nodeWrapper.setNameLabel(nameLabel);
        nodeWrapper.setStatusLabel(statusLabel);

        if (!deployed) {
            rectangle.setOpacity(NOT_DEPLOYED_NODE_OPACITY);
            rectangle.setStyle("-fx-stroke-width: 1; -fx-stroke: black; -fx-stroke-dash-array: 2 2 2 2;");
        }

        if (nodeConfig.hasButton()) {
            Shape nodeButton = CanvasHelper.createNodeButton(rectangle, node.getId());
            nodeWrapper.setNodeButton(nodeButton);
            rootPane.getChildren().add(nodeButton);
            if (!deployed)
                nodeButton.setDisable(true);
        }

        if (nodeConfig.getInputs() > 0) {
            Circle inputHandle = CanvasHelper.createInputHandle(rectangle);
            nodeWrapper.setInputHandle(inputHandle);
            rootPane.getChildren().add(inputHandle);

            inputHandle.setOnMousePressed(event -> {
                if (connectingNode == null) {
                    currentConnectionType = ConnectionType.INPUT_TO_OUTPUT;
                    connectingNode = node;
                    connectingWire = CanvasHelper.createConnectionCurve();
                    updateCurve(inputHandle.getCenterX(), inputHandle.getCenterY(), inputHandle.getCenterX(), inputHandle.getCenterY(), connectingWire);
                    rootPane.getChildren().add(connectingWire);
                }
            });
            inputHandle.setOnMouseReleased(event -> {
                if (connectingNode != null && currentConnectionType.equals(ConnectionType.INPUT_TO_OUTPUT)) {
                    double startX = connectingWire.getStartX();
                    double startY = connectingWire.getStartY();
                    nodeWrappers.values().stream()
                            .filter(nw -> nw.getOutputHandle() != null && nw.getOutputHandle().contains(startX, startY))
                            .limit(1).forEach(nw -> workflowService.connectNodes(nw.getNode(), connectingNode));
                    resetConnectionMode();
                }
            });
            inputHandle.setOnMouseDragged(event -> {
                if (connectingWire != null) {
                    updateCurve(event.getX(), event.getY(), connectingWire.getEndX(), connectingWire.getEndY(), connectingWire);
                    sortChildren();
                }
            });
        }

        if (nodeConfig.getOutputs() > 0) {
            Circle outputHandle = CanvasHelper.createOutputHandle(rectangle);
            nodeWrapper.setOutputHandle(outputHandle);
            rootPane.getChildren().add(outputHandle);

            outputHandle.setOnMousePressed(event -> {
                if (connectingNode == null) {
                    currentConnectionType = ConnectionType.OUTPUT_TO_INPUT;
                    connectingNode = node;
                    connectingWire = CanvasHelper.createConnectionCurve();
                    updateCurve(outputHandle.getCenterX(), outputHandle.getCenterY(), outputHandle.getCenterX(), outputHandle.getCenterY(), connectingWire);
                    rootPane.getChildren().add(connectingWire);
                }
            });
            outputHandle.setOnMouseReleased(event -> {
                if (connectingNode != null && currentConnectionType.equals(ConnectionType.OUTPUT_TO_INPUT)) {
                    double endX = connectingWire.getEndX();
                    double endY = connectingWire.getEndY();
                    nodeWrappers.values().stream()
                            .filter(nw -> nw.getInputHandle() != null && nw.getInputHandle().contains(endX, endY))
                            .limit(1).forEach(nw -> workflowService.connectNodes(connectingNode, nw.getNode()));
                    resetConnectionMode();
                }
            });
            outputHandle.setOnMouseDragged(event -> {
                if (connectingWire != null) {
                    updateCurve(connectingWire.getStartX(), connectingWire.getStartY(), event.getX(), event.getY(), connectingWire);
                    sortChildren();
                }
            });
        }


        closeHandle.setOnMouseClicked(event -> {
            workflowService.deleteNode(node);
        });

        setUpDragging(nodeWrapper);

        rectangle.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                InterfaceHelper.showNodeEditMenu(nodeWrapper, ((Node)event.getSource()).getScene().getWindow());
            }
        });

        nodeWrappers.put(node, nodeWrapper);

        nodeWrapper.init();
    }

    private void resetConnectionMode() {
        if (connectingWire != null)
            rootPane.getChildren().remove(connectingWire);
        connectingWire = null;
        connectingNode = null;
        currentConnectionType = null;
    }

    private void setUpDragging(NodeWrapper nodeWrapper) {
        Wrapper<Point2D> mouseDelta = new Wrapper<>();

        Rectangle rectangle = nodeWrapper.getRectangle();
        Text nameLabel = nodeWrapper.getNameLabel();
        NodeDTO node = nodeWrapper.getNode();

        EventHandler<? super MouseEvent> dragHandler = event -> {
            if (mouseDelta.value != null) {
                double newX = event.getX() - mouseDelta.value.getX();
                double newY = event.getY() - mouseDelta.value.getY();

                newX = Math.round(newX / CANVAS_CELL_SIZE) * CANVAS_CELL_SIZE;
                newY = Math.round(newY / CANVAS_CELL_SIZE) * CANVAS_CELL_SIZE;

                if (newX < 0) newX = 0;
                if (newY < 0) newY = 0;

                rectangle.setX(newX);
                rectangle.setY(newY);
                node.setX(newX);
                node.setY(newY);

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
        };

        for (Shape shape : Arrays.asList(rectangle, nameLabel)) {
            shape.setOnDragDetected(event -> {
                shape.getParent().setCursor(Cursor.CLOSED_HAND);
                Point2D localToScene = rectangle.localToScene(rectangle.getX(), rectangle.getY());
                mouseDelta.value = new Point2D(event.getSceneX(), event.getSceneY()).subtract(localToScene);
            });

            shape.setOnMouseReleased(event -> {
                shape.getParent().setCursor(Cursor.DEFAULT);
                mouseDelta.value = null ;
            });

            shape.setOnMouseDragged(dragHandler);
        }

    }

    public void destroy() {
        nodeWrappers.values().forEach(NodeWrapper::destroy);
        nodeWrappers.clear();
    }

    private enum ConnectionType {
        OUTPUT_TO_INPUT,
        INPUT_TO_OUTPUT
    }

    private void sortChildren() {
        ObservableList<Node> currentChildren = FXCollections.observableArrayList(rootPane.getChildren());
        ObservableList<Node> newChildren = FXCollections.observableArrayList();

        newChildren.addAll(currentChildren.stream().filter(node -> node instanceof CubicCurve).collect(Collectors.toList()));

        for (NodeWrapper wrapper : nodeWrappers.values()) {
            newChildren.add(wrapper.getRectangle());
            newChildren.add(wrapper.getCloseHandle());
            if (wrapper.getOutputHandle() != null)
                newChildren.add(wrapper.getOutputHandle());
            if (wrapper.getInputHandle() != null)
                newChildren.add(wrapper.getInputHandle());
            if (wrapper.getNodeButton() != null)
                newChildren.add(wrapper.getNodeButton());
            newChildren.add(wrapper.getNameLabel());
            newChildren.add(wrapper.getStatusLabel());
        }

        rootPane.getChildren().setAll(newChildren);
    }

}
