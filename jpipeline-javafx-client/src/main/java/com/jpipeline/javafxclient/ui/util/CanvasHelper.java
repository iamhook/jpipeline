package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.javafxclient.service.NodeService;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static com.jpipeline.javafxclient.Consts.HANDLE_RADIUS;
import static com.jpipeline.javafxclient.Consts.NODE_BUTTON_SIZE;

public class CanvasHelper {

    public static CubicCurve createConnectionCurve() {
        CubicCurve curve = new CubicCurve();
        curve.getStyleClass().add("curve");
        return curve;
    }

    public static Rectangle createNodeRectangle(Paint fill) {
        Rectangle rectangle = new Rectangle();
        rectangle.setFill(fill);
        rectangle.getStyleClass().add("node-rectangle");
        return rectangle;
    }

    public static Text createNameLabel(String name, Rectangle rectangle) {
        Text nameLabel = new Text(name);
        nameLabel.xProperty().bind(rectangle.xProperty().add((rectangle.getWidth() - nameLabel.getLayoutBounds().getWidth())/2));
        nameLabel.yProperty().bind(rectangle.yProperty().add(nameLabel.getLayoutBounds().getHeight()));
        nameLabel.getStyleClass().add("name-label");
        return nameLabel;
    }

    public static Text createStatusLabel(Rectangle rectangle) {
        Text statusLabel = new Text();
        statusLabel.xProperty().bind(rectangle.xProperty());
        statusLabel.yProperty().bind(rectangle.yProperty().add(rectangle.heightProperty().add(statusLabel.getLayoutBounds().getHeight())));
        statusLabel.getStyleClass().add("status-label");
        return statusLabel;
    }

    public static Shape createNodeButton(Rectangle rectangle, String nodeId) {
        Rectangle button = new Rectangle(NODE_BUTTON_SIZE, NODE_BUTTON_SIZE);
        button.xProperty().bind(rectangle.xProperty().subtract(3));
        button.yProperty().bind(rectangle.yProperty().add(rectangle.heightProperty()).subtract(button.getHeight() - 3));
        button.getStyleClass().add("node-button");
        button.setOnMouseClicked(event -> NodeService.pressButton(nodeId));
        return button;
    }

    public static Circle createCloseHandle(Rectangle rectangle) {
        return createHandle(rectangle.getWidth(), rectangle.getTranslateY(), rectangle);
    }

    public static Circle createInputHandle(Rectangle rectangle) {
        return createHandle(rectangle.getTranslateX(), rectangle.getHeight()/2, rectangle);
    }

    public static Circle createOutputHandle(Double x, Double y, Rectangle rectangle) {
        return createHandle(x, y, rectangle);
    }

    private static Circle createHandle(Double x, Double y, Rectangle rectangle) {
        Circle handle = new Circle(HANDLE_RADIUS, Color.WHITE);
        handle.centerXProperty().bind(rectangle.xProperty().add(x));
        handle.centerYProperty().bind(rectangle.yProperty().add(y));
        handle.getStyleClass().add("node-handle");
        return handle;
    }


}
