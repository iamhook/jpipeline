package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.javafxclient.service.NodeService;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import static com.jpipeline.javafxclient.Consts.HANDLE_RADIUS;
import static com.jpipeline.javafxclient.Consts.NODE_BUTTON_SIZE;

public class CanvasHelper {

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
        rectangle.setCursor(Cursor.OPEN_HAND);
        rectangle.setArcWidth(7);
        rectangle.setArcHeight(7);
        return rectangle;
    }

    public static Text createNameLabel(String name, Rectangle rectangle) {
        Text nameLabel = new Text(name);
        nameLabel.setCursor(Cursor.OPEN_HAND);
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

    public static Shape createNodeButton(Rectangle rectangle, String nodeId) {
        Rectangle button = new Rectangle(NODE_BUTTON_SIZE, NODE_BUTTON_SIZE);
        button.setArcHeight(5);
        button.setArcWidth(5);
        button.setStroke(Color.GRAY);
        button.setCursor(Cursor.HAND);
        button.xProperty().bind(rectangle.xProperty().subtract(3));
        button.yProperty().bind(rectangle.yProperty().add(rectangle.heightProperty()).subtract(button.getHeight() - 3));

        button.setFill(Color.WHITE);
        button.setOnMouseEntered(event -> button.setFill(Color.LIGHTGREY));
        button.setOnMouseExited(event -> button.setFill(Color.WHITE));
        button.setOnMouseClicked(event -> NodeService.pressButton(nodeId));

        return button;
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
