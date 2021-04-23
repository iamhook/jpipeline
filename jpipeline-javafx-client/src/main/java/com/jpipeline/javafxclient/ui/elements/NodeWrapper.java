package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.service.NodeService;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
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

    private NodeDTO node;

    private Pane parent;
    private Rectangle rectangle;

    private Text nameLabel;
    private Text statusLabel;

    private Circle outputHandle;
    private Circle inputHandle;
    private Circle closeHandle;

    private List<Path> outputs = new ArrayList<>();
    private List<Path> inputs = new ArrayList<>();

    Disposable statusSubscription;

    public NodeWrapper(NodeDTO node) {
        this.node = node;
    }

    public void init() {
        try {
            statusSubscription = NodeService.getStatusStream(node.getId())
                    .onBackpressureDrop()
                    .limitRate(1)
                    .delayElements(Duration.ofMillis(100))
                    .subscribe(nodeStatus -> statusLabel.setText(nodeStatus.getStatus()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addOutput(Path path) {
        outputs.add(path);
    }
    public void addInput(Path path) {
        inputs.add(path);
    }

    public void destroy() {
        if (!statusSubscription.isDisposed())
            statusSubscription.dispose();

        getOutputs().forEach(path -> parent.getChildren().remove(path));
        getInputs().forEach(path -> parent.getChildren().remove(path));
        parent.getChildren().remove(outputHandle);
        parent.getChildren().remove(inputHandle);
        parent.getChildren().remove(closeHandle);
        parent.getChildren().remove(rectangle);
        parent.getChildren().remove(nameLabel);
        parent.getChildren().remove(statusLabel);
    }

}
