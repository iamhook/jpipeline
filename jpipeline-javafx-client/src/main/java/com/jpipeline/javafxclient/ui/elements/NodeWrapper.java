package com.jpipeline.javafxclient.ui.elements;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class NodeWrapper {


    Pane parent;
    Rectangle rectangle;

    Circle outputHandle;
    Circle inputHandle;
    Circle closeHandle;

    List<Path> outputs = new ArrayList<>();
    List<Path> inputs = new ArrayList<>();


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
    }

}
