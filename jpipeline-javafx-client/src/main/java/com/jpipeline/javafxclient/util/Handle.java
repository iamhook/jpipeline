package com.jpipeline.javafxclient.util;

import com.jpipeline.javafxclient.service.ViewWorkflowService;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.jpipeline.javafxclient.Consts.HANDLE_RADIUS;

public abstract class Handle extends Circle {

    @Getter
    private List<CubicWire> wires = new ArrayList<>();

    @Getter
    protected ViewWorkflowService.NodeWrapper nodeWrapper;

    public Handle(ViewWorkflowService.NodeWrapper nodeWrapper) {
        super(HANDLE_RADIUS, Color.WHITE);
        this.nodeWrapper = nodeWrapper;
        this.getStyleClass().add("node-handle");
        updatePosition();
    }

    public void addWire(CubicWire wire) {
        wires.add(wire);
    }

    public void removeWire(CubicWire wire) {
        wires.remove(wire);
    }

    private void updatePosition(Circle handle, Double x, Double y) {
        Rectangle rectangle = nodeWrapper.getRectangle();
        handle.centerXProperty().bind(rectangle.xProperty().add(x));
        handle.centerYProperty().bind(rectangle.yProperty().add(y));
    }

    protected abstract void updatePosition();

    protected void updatePosition(ObservableDoubleValue x, ObservableDoubleValue y) {
        centerXProperty().bind(nodeWrapper.getRectangle().xProperty().add(x));
        centerYProperty().bind(nodeWrapper.getRectangle().yProperty().add(y));
    }
}
