package com.jpipeline.javafxclient.ui.util;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

import static com.jpipeline.javafxclient.Consts.HANDLE_RADIUS;

public class ViewHelper {

    public static CubicCurve createConnectionCurve() {
        CubicCurve curve = new CubicCurve();
        curve.setStroke(Color.GRAY);
        curve.setStrokeWidth(3);
        curve.setStrokeLineCap(StrokeLineCap.ROUND);
        curve.setFill(Color.TRANSPARENT);
        return curve;
    }

    public static Rectangle createNodeRectangle(Paint fill) {
        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(Color.BLACK);
        rectangle.setFill(fill);
        return rectangle;
    }

    public static Text createNameLabel(String name, Rectangle rectangle) {
        Text nameLabel = new Text(name);
        nameLabel.xProperty().bind(rectangle.xProperty().add((rectangle.getWidth() - nameLabel.getLayoutBounds().getWidth())/2));
        nameLabel.yProperty().bind(rectangle.yProperty().add(nameLabel.getLayoutBounds().getHeight()));
        return nameLabel;
    }

    public static Text createStatusLabel(Rectangle rectangle) {
        Text statusLabel = new Text();
        statusLabel.xProperty().bind(rectangle.xProperty());
        statusLabel.yProperty().bind(rectangle.yProperty().add(rectangle.heightProperty().add(statusLabel.getLayoutBounds().getHeight())));
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
