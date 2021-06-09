package com.jpipeline.javafxclient.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.entity.Node;
import com.jpipeline.javafxclient.service.NodeService;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class NodeWrapper {

    private static ObjectMapper OM = new ObjectMapper();

    private NodeDTO node;

    private Pane parent;
    private Rectangle rectangle;

    private Text nameLabel;
    private Text statusLabel;

    private List<List<CubicCurve>> outputs = new ArrayList<>();
    private List<Circle> outputHandles = new ArrayList<>();
    private Circle inputHandle;
    private Shape nodeButton;

    private List<CubicCurve> inputWires = new ArrayList<>();

    Disposable statusSubscription;

    public NodeWrapper(NodeDTO node) {
        this.node = node;
    }

    public void init() {
        try {
            statusSubscription = NodeService.getSignalStream()
                    .filter(nodeSignal -> nodeSignal.getNodeId().toString().equals(node.getId()))
                    .filter(nodeSignal -> nodeSignal.getType().equals(Node.SignalType.STATUS))
                    .map(nodeSignal -> OM.convertValue(nodeSignal.getBody(), Node.NodeStatus.class))
                    .onBackpressureDrop()
                    .limitRate(1)
                    .delayElements(Duration.ofMillis(100))
                    .subscribe(nodeStatus -> Platform.runLater(() -> statusLabel.setText(nodeStatus.getStatus())));
        } catch (Exception e) {
            e.printStackTrace();
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

        getOutputs().forEach(path -> parent.getChildren().remove(path));
        getInputWires().forEach(path -> parent.getChildren().remove(path));
        for (Circle outputHandle : outputHandles) {
            parent.getChildren().remove(outputHandle);
        }
        for (List<CubicCurve> output : outputs) {
            for (CubicCurve curve : output) {
                parent.getChildren().remove(curve);
            }
        }
        parent.getChildren().remove(inputHandle);
        parent.getChildren().remove(rectangle);
        parent.getChildren().remove(nameLabel);
        parent.getChildren().remove(statusLabel);
        parent.getChildren().remove(nodeButton);
    }

}
