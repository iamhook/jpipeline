package com.jpipeline.javafxclient.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.util.CanvasHelper;
import com.jpipeline.javafxclient.ui.util.InterfaceHelper;
import com.jpipeline.javafxclient.ui.util.Wrapper;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.jpipeline.javafxclient.Consts.*;

public class ViewWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ViewWorkflowService.class);

    private static ObjectMapper OM = new ObjectMapper();

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
                .flatMap(Collection::stream)
                .filter(p -> toNodeWrapper.getInputWires().contains(p))
                .findFirst().orElse(null);

        fromNodeWrapper.getOutputs().remove(curve);
        toNodeWrapper.getInputWires().remove(curve);
        rootPane.getChildren().remove(curve);
    }

    public void deleteNode(NodeDTO node) {
        NodeWrapper nodeWrapper = nodeWrappers.get(node);
        nodeWrapper.destroy();
        nodeWrappers.remove(node);
    }

    public void connectNodes(NodeDTO fromNode, NodeDTO toNode, int output) {
        NodeWrapper fromNodeWrapper = nodeWrappers.get(fromNode);
        NodeWrapper toNodeWrapper = nodeWrappers.get(toNode);
        Circle fromHandle = fromNodeWrapper.getOutputHandles().get(output);
        Circle toHandle = toNodeWrapper.getInputHandle();

        double fromX = fromHandle.getCenterX();
        double fromY = fromHandle.getCenterY();
        double toX = toHandle.getCenterX();
        double toY = toHandle.getCenterY();

        CubicCurve curve = CanvasHelper.createConnectionCurve();

        updateCurve(fromX, fromY, toX, toY, curve);

        curve.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                workflowService.disconnectNodes(fromNode, toNode, output);
            }
        });

        fromNodeWrapper.addOutputWire(curve, output);
        toNodeWrapper.addInputWire(curve);

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

    public void createNode(NodeDTO node, boolean isDeployed) {
        NodeWrapper nodeWrapper = new NodeWrapper(node, isDeployed);
        nodeWrappers.put(node, nodeWrapper);
        nodeWrapper.init();
        sortChildren();
    }

    public void initInputHandle(Circle inputHandle, NodeWrapper nodeWrapper) {
        inputHandle.setOnMousePressed(event -> {
            if (connectingNode == null) {
                currentConnectionType = ViewWorkflowService.ConnectionType.INPUT_TO_OUTPUT;
                connectingNode = nodeWrapper.getNode();
                connectingWire = CanvasHelper.createConnectionCurve();
                rootPane.getChildren().add(connectingWire);
                updateCurve(inputHandle.getCenterX(), inputHandle.getCenterY(), inputHandle.getCenterX(), inputHandle.getCenterY(), connectingWire);
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
                            workflowService.connectNodes(nw.getNode(), connectingNode, i);
                        }
                        i++;
                    }
                });
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

    public void initOutputHandle(Circle outputHandle, int outputIndex, NodeWrapper nodeWrapper) {
        outputHandle.setOnMousePressed(event -> {
            if (connectingNode == null) {
                currentConnectionType = ConnectionType.OUTPUT_TO_INPUT;
                connectingNode = nodeWrapper.getNode();
                connectingWire = CanvasHelper.createConnectionCurve();
                rootPane.getChildren().add(connectingWire);
                updateCurve(outputHandle.getCenterX(), outputHandle.getCenterY(), outputHandle.getCenterX(), outputHandle.getCenterY(), connectingWire);
            }
        });
        outputHandle.setOnMouseReleased(event -> {
            if (connectingNode != null && currentConnectionType.equals(ConnectionType.OUTPUT_TO_INPUT)) {
                double endX = connectingWire.getEndX();
                double endY = connectingWire.getEndY();
                nodeWrappers.values().stream()
                        .filter(nw -> nw.getInputHandle() != null && nw.getInputHandle().contains(endX, endY))
                        .limit(1).forEach(nw -> workflowService.connectNodes(connectingNode, nw.getNode(), outputIndex));
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

                for (CubicCurve curve : nodeWrapper.getInputWires()) {
                    Circle to = nodeWrapper.getInputHandle();
                    double fromX = curve.getStartX();
                    double fromY = curve.getStartY();
                    double toX = to.getCenterX();
                    double toY = to.getCenterY();

                    updateCurve(fromX, fromY, toX, toY, curve);
                }

                for (int i = 0; i < nodeWrapper.getOutputHandles().size(); i++) {
                    Circle from = nodeWrapper.getOutputHandles().get(i);
                    for (CubicCurve curve : nodeWrapper.getOutputWires(i)) {
                        double fromX = from.getCenterX();
                        double fromY = from.getCenterY();
                        double toX = curve.getEndX();
                        double toY = curve.getEndY();

                        updateCurve(fromX, fromY, toX, toY, curve);
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

        private List<List<CubicCurve>> outputs = new ArrayList<>();
        private List<Circle> outputHandles = new ArrayList<>();
        private Circle inputHandle;
        private Shape nodeButton;

        private List<CubicCurve> inputWires = new ArrayList<>();

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
                    workflowService.deleteNode(node);
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
            inputHandle = CanvasHelper.createInputHandle(rectangle);
            initInputHandle(inputHandle, this);
            rootPane.getChildren().add(inputHandle);
        }

        public void addOutput() {
            Circle outputHandle = CanvasHelper.createOutputHandle();
            outputHandles.add(outputHandle);
            outputs.add(new ArrayList<>());
            initOutputHandle(outputHandle, outputHandles.size() - 1, this);
            rootPane.getChildren().add(outputHandle);
        }

        public void fixOutputHandlesPositions() {
            for (int i = 0; i < outputs.size(); i++) {
                Circle handle = outputHandles.get(i);
                CanvasHelper.updateOutputHandlePosition(handle, outputs.size(), i, rectangle);
            }
        }

        public List<CubicCurve> getOutputWires(int output) {
            return outputs.get(output);
        }

        public void addOutputWire(CubicCurve curve, int output) {
            outputs.get(output).add(curve);
        }

        public void addInputWire(CubicCurve curve) {
            inputWires.add(curve);
        }

        public void destroy() {
            if (statusSubscription != null && !statusSubscription.isDisposed())
                statusSubscription.dispose();

            getOutputs().forEach(path -> rootPane.getChildren().remove(path));
            getInputWires().forEach(path -> rootPane.getChildren().remove(path));
            for (Circle outputHandle : outputHandles) {
                rootPane.getChildren().remove(outputHandle);
            }
            for (List<CubicCurve> output : outputs) {
                for (CubicCurve curve : output) {
                    rootPane.getChildren().remove(curve);
                }
            }
            rootPane.getChildren().remove(inputHandle);
            rootPane.getChildren().remove(rectangle);
            rootPane.getChildren().remove(nameLabel);
            rootPane.getChildren().remove(statusLabel);
            rootPane.getChildren().remove(nodeButton);
        }

    }

}
