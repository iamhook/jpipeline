package com.jpipeline.javafxclient.ui.elements;

import javafx.scene.shape.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class NodeWrapper {

    Rectangle rectangle;

    Circle outputHandle;
    Circle inputHandle;

    List<Path> outputs = new ArrayList<>();
    List<Path> inputs = new ArrayList<>();


    public void addOutput(Path path) {
        outputs.add(path);
    }
    public void addInput(Path path) {
        inputs.add(path);
    }

}
