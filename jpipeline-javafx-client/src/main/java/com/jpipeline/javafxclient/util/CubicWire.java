package com.jpipeline.javafxclient.util;

import javafx.scene.shape.CubicCurve;
import lombok.Getter;

import static com.jpipeline.javafxclient.Consts.NODE_BASE_HEIGHT;
import static com.jpipeline.javafxclient.Consts.NODE_BASE_WIDTH;

public class CubicWire extends CubicCurve {

    @Getter
    private OutputHandle fromHandle;
    @Getter
    private InputHandle toHandle;

    public CubicWire(OutputHandle fromHandle, InputHandle toHandle) {
        this.fromHandle = fromHandle;
        this.toHandle = toHandle;

        getStyleClass().add("curve");

        updatePosition();
    }

    public void updatePosition() {
        double fromX = fromHandle.getCenterX();
        double fromY = fromHandle.getCenterY();
        double toX = toHandle.getCenterX();
        double toY = toHandle.getCenterY();

        updatePosition(fromX, fromY, toX, toY, this);
    }

    public static void updatePosition(double fromX, double fromY, double toX,
                                      double toY, CubicCurve curve) {
        double yControlOffset = toY > fromY ? NODE_BASE_HEIGHT / 2f: NODE_BASE_HEIGHT / -2f;
        double xControlOffset = NODE_BASE_WIDTH * 1.5;

        double sqrt = Math.sqrt(Math.pow(Math.abs(toX - fromX), 2) + Math.pow(Math.abs(toY - fromY), 2));
        xControlOffset = Math.min(sqrt, xControlOffset);

        curve.setStartX(fromX);
        curve.setStartY(fromY);
        curve.setControlX1(fromX + xControlOffset);
        curve.setControlY1(fromY + yControlOffset);
        curve.setControlX2(toX - xControlOffset);
        curve.setControlY2(toY - yControlOffset);
        curve.setEndX(toX);
        curve.setEndY(toY);
    }

    public void destroy() {
        fromHandle.removeWire(this);
        toHandle.removeWire(this);
    }

}
