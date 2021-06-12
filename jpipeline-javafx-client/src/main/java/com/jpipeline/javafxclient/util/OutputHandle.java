package com.jpipeline.javafxclient.util;

import com.jpipeline.javafxclient.service.ViewWorkflowService;
import javafx.scene.shape.Rectangle;

public class OutputHandle extends Handle {


    public OutputHandle(ViewWorkflowService.NodeWrapper nodeWrapper) {
        super(nodeWrapper);
    }

    @Override
    public void updatePosition() {
        Rectangle rectangle = nodeWrapper.getRectangle();
        int outputs = nodeWrapper.getOutputHandles().size();
        int outputIndex = nodeWrapper.getOutputHandles().indexOf(this);
        updatePosition(rectangle.widthProperty(), rectangle.heightProperty().divide(outputs + 1).multiply((outputIndex+1)));
    }

}
