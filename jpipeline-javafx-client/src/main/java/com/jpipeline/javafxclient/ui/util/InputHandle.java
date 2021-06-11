package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.javafxclient.ui.ViewWorkflowService;
import javafx.scene.shape.Rectangle;

public class InputHandle extends Handle {

    public InputHandle(ViewWorkflowService.NodeWrapper nodeWrapper) {
        super(nodeWrapper);
    }

    @Override
    protected void updatePosition() {
        Rectangle rectangle = nodeWrapper.getRectangle();
        updatePosition(rectangle.getTranslateX(), rectangle.getHeight()/2);
    }
}
