package com.jpipeline.javafxclient.ui.elements;

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

    private Circle outputHandle;
    private Circle inputHandle;
    private Circle closeHandle;
    private Shape nodeButton;

    private List<CubicCurve> outputs = new ArrayList<>();
    private List<CubicCurve> inputs = new ArrayList<>();

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

    public void addOutput(CubicCurve curve) {
        outputs.add(curve);
    }
    public void addInput(CubicCurve curve) {
        inputs.add(curve);
    }

    public void destroy() {
        if (statusSubscription != null && !statusSubscription.isDisposed())
            statusSubscription.dispose();

        getOutputs().forEach(path -> parent.getChildren().remove(path));
        getInputs().forEach(path -> parent.getChildren().remove(path));
        parent.getChildren().remove(outputHandle);
        parent.getChildren().remove(inputHandle);
        parent.getChildren().remove(closeHandle);
        parent.getChildren().remove(rectangle);
        parent.getChildren().remove(nameLabel);
        parent.getChildren().remove(statusLabel);
        parent.getChildren().remove(nodeButton);
    }

}
