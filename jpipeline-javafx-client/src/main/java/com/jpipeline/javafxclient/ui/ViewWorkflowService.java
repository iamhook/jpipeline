package com.jpipeline.javafxclient.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.util.*;
import javafx.application.Platform;
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
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.jpipeline.javafxclient.Consts.*;

public class ViewWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ViewWorkflowService.class);

    private static ObjectMapper OM = new ObjectMapper();

    private Pane rootPane;

    private final double canvasHeight = 2000;
    private final double canvasWidth = 2000;

    private Map<String, NodeWrapper> nodeWrappers = new HashMap<>();

    private NodeWrapper connectingNode;
    private CubicCurve connectingWire;
    private ConnectionType currentConnectionType;

    private ModelWorkflowService modelService;

    public ViewWorkflowService(Pane rootPane, WorkflowConfig workflowConfig) {
        this.rootPane = rootPane;
        this.modelService = new ModelWorkflowService(workflowConfig);

        rootPane.setPrefHeight(canvasHeight);
        rootPane.setPrefWidth(canvasWidth);
        setClip();

        nodeWrappers = workflowConfig.getNodes().stream().map(node -> createNode(node, true)).collect(Collectors.toMap(w -> w.getNode().getId(), w -> w));
        nodeWrappers.forEach((id, wrapper) -> {
            int i = 0;
            for (Set<String> wires : wrapper.getNode().getOutputs()) {
                for (String wire : wires) {
                    connectNodes(wrapper, nodeWrappers.get(wire), i);
                }
                i++;
            }
        });
    }

    private void setClip() {
        Rectangle clip = new Rectangle(canvasWidth, canvasHeight);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        rootPane.setClip(clip);
    }

    public void deleteWire(CubicWire wire) {
        OutputHandle fromHandle = wire.getFromHandle();
        InputHandle toHandle = wire.getToHandle();
        ViewWorkflowService.NodeWrapper fromWrapper = fromHandle.getNodeWrapper();
        ViewWorkflowService.NodeWrapper toWrapper = toHandle.getNodeWrapper();
        NodeDTO fromNode = fromWrapper.getNode();
        NodeDTO toNode = toWrapper.getNode();
        int output = fromWrapper.getOutputHandles().indexOf(fromHandle);
        modelService.deleteWire(fromNode, toNode, output);
        wire.destroy();
        rootPane.getChildren().remove(wire);
    }

    public void deleteNode(NodeWrapper wrapper) {
        modelService.deleteNode(wrapper.node);
        wrapper.destroy();
        nodeWrappers.remove(wrapper.node.getId());
    }

    public void connectNodes(NodeWrapper fromNode, NodeWrapper toNode, int output) {
        modelService.connectNodes(fromNode.getNode(), toNode.getNode(), output);

        OutputHandle fromHandle = fromNode.getOutputHandles().get(output);
        InputHandle toHandle = toNode.getInputHandle();

        CubicWire wire = new CubicWire(fromHandle, toHandle);

        wire.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                deleteWire(wire);
            }
        });

        fromHandle.getWires().add(wire);
        toHandle.getWires().add(wire);


        rootPane.getChildren().add(wire);

        sortChildren();

        connectingNode = null;
        currentConnectionType = null;
    }

    public NodeWrapper createNode(NodeDTO node, boolean isDeployed) {
        NodeWrapper nodeWrapper = new NodeWrapper(node, isDeployed);
        nodeWrappers.put(node.getId(), nodeWrapper);
        nodeWrapper.init();
        sortChildren();
        if (!isDeployed)
            modelService.createNode(node);
        return nodeWrapper;
    }

    public void initInputHandle(Circle inputHandle, NodeWrapper nodeWrapper) {
        inputHandle.setOnMousePressed(event -> {
            if (connectingNode == null) {
                currentConnectionType = ViewWorkflowService.ConnectionType.INPUT_TO_OUTPUT;
                connectingNode = nodeWrapper;
                connectingWire = new CubicCurve();
                connectingWire.getStyleClass().add("curve");
                rootPane.getChildren().add(connectingWire);
                CubicWire.updatePosition(inputHandle.getCenterX(), inputHandle.getCenterY(), inputHandle.getCenterX(), inputHandle.getCenterY(), connectingWire);
            }
        });
        inputHandle.setOnMouseReleased(event -> {
            if (connectingNode != null && currentConnectionType.equals(ViewWorkflowService.ConnectionType.INPUT_TO_OUTPUT)) {
                double startX = connectingWire.getStartX();
                double startY = connectingWire.getStartY();
                nodeWrappers.values().forEach(nw -> {
                    int i = 0;
                    for (Circle outputHandle : nw.getOutputHandles()) {
                        if (outputHandle.contains(startX, startY)) {
                            connectNodes(nw, connectingNode, i);
                        }
                        i++;
                    }
                });
                resetConnectionMode();
            }
        });
        inputHandle.setOnMouseDragged(event -> {
            if (connectingWire != null) {
                CubicWire.updatePosition(event.getX(), event.getY(), connectingWire.getEndX(), connectingWire.getEndY(), connectingWire);
                sortChildren();
            }
        });
    }

    public void initOutputHandle(Circle outputHandle, int outputIndex, NodeWrapper nodeWrapper) {
        outputHandle.setOnMousePressed(event -> {
            if (connectingNode == null) {
                currentConnectionType = ConnectionType.OUTPUT_TO_INPUT;
                connectingNode = nodeWrapper;
                connectingWire = new CubicCurve();
                connectingWire.getStyleClass().add("curve");
                rootPane.getChildren().add(connectingWire);
                CubicWire.updatePosition(outputHandle.getCenterX(), outputHandle.getCenterY(), outputHandle.getCenterX(), outputHandle.getCenterY(), connectingWire);
            }
        });
        outputHandle.setOnMouseReleased(event -> {
            if (connectingNode != null && currentConnectionType.equals(ConnectionType.OUTPUT_TO_INPUT)) {
                double endX = connectingWire.getEndX();
                double endY = connectingWire.getEndY();
                nodeWrappers.values().stream()
                        .filter(nw -> nw.getInputHandle() != null && nw.getInputHandle().contains(endX, endY))
                        .limit(1).forEach(nw -> {
                    connectNodes(connectingNode, nw, outputIndex);
                });
                resetConnectionMode();
            }
        });
        outputHandle.setOnMouseDragged(event -> {
            if (connectingWire != null) {
                CubicWire.updatePosition(connectingWire.getStartX(), connectingWire.getStartY(), event.getX(), event.getY(), connectingWire);
                sortChildren();
            }
        });
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

                if (nodeWrapper.getInputHandle() != null) {
                    for (CubicWire wire : nodeWrapper.getInputHandle().getWires()) {
                        wire.updatePosition();
                    }
                }

                for (OutputHandle outputHandle : nodeWrapper.getOutputHandles()) {
                    for (CubicWire wire : outputHandle.getWires()) {
                        wire.updatePosition();
                    }
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
            if (wrapper.getOutputHandles() != null)
                newChildren.addAll(wrapper.getOutputHandles());
            if (wrapper.getInputHandle() != null)
                newChildren.add(wrapper.getInputHandle());
            if (wrapper.getNodeButton() != null)
                newChildren.add(wrapper.getNodeButton());
            newChildren.add(wrapper.getNameLabel());
            newChildren.add(wrapper.getStatusLabel());
        }

        rootPane.getChildren().setAll(newChildren);
    }


    @Setter
    @Getter
    public class NodeWrapper {

        private NodeDTO node;

        private Rectangle rectangle;

        private Text nameLabel;
        private Text statusLabel;

        private List<OutputHandle> outputHandles = new ArrayList<>();
        private InputHandle inputHandle;
        private Shape nodeButton;

        Disposable statusSubscription;

        public NodeWrapper(NodeDTO node, boolean isDeployed) {
            this.node = node;

            if (node.getX() == null)
                node.setX(DEFAULT_X);
            if (node.getY() == null)
                node.setY(DEFAULT_Y);

            rectangle = CanvasHelper.createNodeRectangle(Paint.valueOf(node.getColor()));

            rectangle.setWidth(NODE_WIDTH);
            rectangle.setHeight(NODE_HEIGHT);
            rectangle.setX(node.getX());
            rectangle.setY(node.getY());

            nameLabel = CanvasHelper.createNameLabel(node.getType(), rectangle);
            statusLabel = CanvasHelper.createStatusLabel(rectangle);

            if (!isDeployed) {
                rectangle.setOpacity(NOT_DEPLOYED_NODE_OPACITY);
                rectangle.setStyle("-fx-stroke-width: 1; -fx-stroke: black; -fx-stroke-dash-array: 2 2 2 2;");
            }

            if (node.hasButton()) {
                nodeButton = CanvasHelper.createNodeButton(rectangle, node.getId());
                if (!isDeployed)
                    nodeButton.setDisable(true);
            }

            if (node.hasInput()) {
                addInput();
            }

            if (node.getOutputs().size() > 0) {
                for (int i = 0; i < node.getOutputs().size(); i++) {
                    addOutput();
                }
            }

            fixOutputHandlesPositions();

            setUpDragging(this);

            EventHandler<? super MouseEvent> clickListener = event -> {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    InterfaceHelper.showNodeEditMenu(this, ((Node) event.getSource()).getScene().getWindow());
                } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                    deleteNode(this);
                }
            };

            nameLabel.setOnMouseClicked(clickListener);
            rectangle.setOnMouseClicked(clickListener);
        }

        public void init() {
            try {
                statusSubscription = NodeService.getSignalStream()
                        .filter(nodeSignal -> nodeSignal.getNodeId().toString().equals(node.getId()))
                        .filter(nodeSignal -> nodeSignal.getType().equals(com.jpipeline.common.entity.Node.SignalType.STATUS))
                        .map(nodeSignal -> OM.convertValue(nodeSignal.getBody(), com.jpipeline.common.entity.Node.NodeStatus.class))
                        .onBackpressureDrop()
                        .limitRate(1)
                        .delayElements(Duration.ofMillis(100))
                        .subscribe(nodeStatus -> Platform.runLater(() -> statusLabel.setText(nodeStatus.getStatus())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addInput() {
            inputHandle = new InputHandle(this);
            initInputHandle(inputHandle, this);
            rootPane.getChildren().add(inputHandle);
        }

        public void addOutput() {
            OutputHandle outputHandle = new OutputHandle(this);
            outputHandles.add(outputHandle);
            initOutputHandle(outputHandle, outputHandles.size() - 1, this);
            rootPane.getChildren().add(outputHandle);
        }

        public void removeOutput(int idx) {
            for (CubicWire wire : getOutputHandles().get(idx).getWires()) {
                deleteWire(wire);
            }

            node.getOutputs().remove(idx);
            rootPane.getChildren().remove(outputHandles.remove(idx));
            fixOutputHandlesPositions();

        }

        public void fixOutputHandlesPositions() {
            outputHandles.forEach(OutputHandle::updatePosition);
        }

        public void destroy() {
            ObservableList<Node> children = rootPane.getChildren();
            if (statusSubscription != null && !statusSubscription.isDisposed())
                statusSubscription.dispose();

            for (OutputHandle outputHandle : outputHandles) {
                outputHandle.getWires().forEach(children::remove);
                rootPane.getChildren().remove(outputHandle);
            }
            if (inputHandle != null) {
                inputHandle.getWires().forEach(children::remove);
                children.remove(inputHandle);
            }
            children.remove(rectangle);
            children.remove(nameLabel);
            children.remove(statusLabel);
            children.remove(nodeButton);
        }

    }

}
