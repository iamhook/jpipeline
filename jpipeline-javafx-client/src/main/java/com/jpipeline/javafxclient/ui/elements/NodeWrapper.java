package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.dto.NodeDTO;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;

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

    public NodeWrapper(NodeDTO node) {
        this.node = node;
    }

    public void addOutput(Path path) {
        outputs.add(path);
    }
    public void addInput(Path path) {
        inputs.add(path);
    }

    public void destroy() {
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
