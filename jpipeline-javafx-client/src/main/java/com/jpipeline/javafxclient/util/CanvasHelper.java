package com.jpipeline.javafxclient.util;

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


}
