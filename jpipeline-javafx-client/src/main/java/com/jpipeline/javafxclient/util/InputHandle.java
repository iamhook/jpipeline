package com.jpipeline.javafxclient.util;

import com.jpipeline.javafxclient.service.ViewWorkflowService;
import javafx.scene.shape.Rectangle;

public class InputHandle extends Handle {

    public InputHandle(ViewWorkflowService.NodeWrapper nodeWrapper) {
        super(nodeWrapper);
    }

    @Override
    protected void updatePosition() {
        Rectangle rectangle = nodeWrapper.getRectangle();
        updatePosition(rectangle.translateXProperty(), rectangle.heightProperty().divide(2));
    }
}
