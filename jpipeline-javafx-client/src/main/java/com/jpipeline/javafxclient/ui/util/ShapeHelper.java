package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.common.dto.NodeDTO;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class ShapeHelper {

    private static final int NODE_HEIGHT = 50;
    private static final int NODE_WIDTH = 100;
    private static final double DEFAULT_X = 200;
    private static final double DEFAULT_Y = 200;

    public static Rectangle createNodeRectangle(NodeDTO node) {
        //final double handleRadius = 10 ;

        if (node.getX() == null)
            node.setX(DEFAULT_X);
        if (node.getY() == null)
            node.setY(DEFAULT_Y);

        Rectangle rect = new Rectangle(node.getX(), node.getY(), NODE_WIDTH, NODE_HEIGHT);

        rect.setFill(Paint.valueOf(node.getColor()));

        // move handle:
        //Circle moveHandle = new Circle(handleRadius, Color.GOLD);
        // bind to bottom center of Rectangle:
        /*moveHandle.centerXProperty().bind(rect.xProperty().add(rect.widthProperty().divide(2)));
        moveHandle.centerYProperty().bind(rect.yProperty().add(rect.heightProperty()));

        // force circles to live in same parent as rectangle:
        rect.parentProperty().addListener((obs, oldParent, newParent) -> {
            for (Circle c : Arrays.asList(moveHandle)) {
                Pane currentParent = (Pane)c.getParent();
                if (currentParent != null) {
                    currentParent.getChildren().remove(c);
                }
                ((Pane)newParent).getChildren().add(c);
            }
        });*/

        Wrapper<Point2D> mouseLocation = new Wrapper<>();

        //setUpDragging(moveHandle, mouseLocation);
        setUpDragging(rect, mouseLocation);

        rect.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getX() + deltaX ;
                double newY = rect.getY() + deltaY ;
                rect.setX(newX);
                rect.setY(newY);
                node.setX(newX);
                node.setY(newY);
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        return rect ;
    }

    private static void setUpDragging(Shape shape, Wrapper<Point2D> mouseLocation) {

        shape.setOnDragDetected(event -> {
            shape.getParent().setCursor(Cursor.CLOSED_HAND);
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        shape.setOnMouseReleased(event -> {
            shape.getParent().setCursor(Cursor.DEFAULT);
            mouseLocation.value = null ;
        });
    }

    static class Wrapper<T> { T value ; }


}
