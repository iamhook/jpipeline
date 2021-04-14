package com.jpipeline.javafxclient.ui.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ShapeHelper {

    private static final double HANDLE_RADIUS = 5.0f;


    public static Circle createCloseHandle(Rectangle Rectangle) {
        return createHandle(Rectangle.getWidth(), Rectangle.getTranslateY(), Rectangle);
    }

    public static Circle createInputHandle(Rectangle Rectangle) {
        return createHandle(Rectangle.getTranslateX(), Rectangle.getHeight()/2, Rectangle);
    }

    public static Circle createOutputHandle(Rectangle Rectangle) {
        return createHandle(Rectangle.getWidth(), Rectangle.getHeight()/2, Rectangle);
    }

    private static Circle createHandle(Double x, Double y, Rectangle Rectangle) {
        Circle handle = new Circle(HANDLE_RADIUS, Color.WHITE);
        handle.setStroke(Color.GRAY);
        handle.centerXProperty().bind(Rectangle.xProperty().add(x));
        handle.centerYProperty().bind(Rectangle.yProperty().add(y));

        handle.setOnMouseEntered(event -> {
            handle.setFill(Color.ORANGERED);
        });
        handle.setOnMouseExited(event -> {
            handle.setFill(Color.WHITE);
        });

        return handle;
    }


}
