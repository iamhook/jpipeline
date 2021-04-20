package com.jpipeline.javafxclient.ui.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ViewHelper {

    private static final double HANDLE_RADIUS = 5.0f;

    public static Text createNameLabel(String name, Rectangle rectangle) {
        Text nameLabel = new Text(name);
        nameLabel.xProperty().bind(rectangle.xProperty());
        nameLabel.yProperty().bind(rectangle.yProperty());
        return nameLabel;
    }

    public static Text createStatusLabel(Rectangle rectangle) {
        Text statusLabel = new Text();
        statusLabel.xProperty().bind(rectangle.xProperty());
        statusLabel.yProperty().bind(rectangle.yProperty().add(rectangle.heightProperty().add(statusLabel.prefHeight(1))));
        return statusLabel;
    }

    public static Circle createCloseHandle(Rectangle rectangle) {
        return createHandle(rectangle.getWidth(), rectangle.getTranslateY(), rectangle);
    }

    public static Circle createInputHandle(Rectangle rectangle) {
        return createHandle(rectangle.getTranslateX(), rectangle.getHeight()/2, rectangle);
    }

    public static Circle createOutputHandle(Rectangle rectangle) {
        return createHandle(rectangle.getWidth(), rectangle.getHeight()/2, rectangle);
    }

    private static Circle createHandle(Double x, Double y, Rectangle rectangle) {
        Circle handle = new Circle(HANDLE_RADIUS, Color.WHITE);
        handle.setStroke(Color.GRAY);
        handle.centerXProperty().bind(rectangle.xProperty().add(x));
        handle.centerYProperty().bind(rectangle.yProperty().add(y));

        handle.setOnMouseEntered(event -> {
            handle.setFill(Color.ORANGERED);
        });
        handle.setOnMouseExited(event -> {
            handle.setFill(Color.WHITE);
        });

        return handle;
    }


}
