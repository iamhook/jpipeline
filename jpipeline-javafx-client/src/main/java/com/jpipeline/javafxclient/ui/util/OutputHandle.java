package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.javafxclient.ui.ViewWorkflowService;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class OutputHandle extends Handle {


    public OutputHandle(ViewWorkflowService.NodeWrapper nodeWrapper) {
        super(nodeWrapper);
    }

    @Override
    public void updatePosition() {
        Rectangle rectangle = nodeWrapper.getRectangle();
        int outputs = nodeWrapper.getOutputHandles().size();
        int outputIndex = nodeWrapper.getOutputHandles().indexOf(this);
        updatePosition(rectangle.getWidth(), rectangle.getHeight() / (outputs + 1) * (outputIndex+1));
    }

}
