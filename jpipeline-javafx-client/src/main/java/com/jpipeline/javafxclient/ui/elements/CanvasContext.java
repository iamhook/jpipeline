package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.ui.util.ShapeHelper;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class CanvasContext {

    private Pane rootPane;
    private Canvas canvas;

    private final double canvasHeight;
    private final double canvasWidth;

    public CanvasContext(Pane rootPane) {
        this.rootPane = rootPane;

        canvasHeight = rootPane.getPrefHeight();
        canvasWidth = rootPane.getPrefWidth();

        this.canvas = new Canvas(canvasWidth, canvasHeight);
        rootPane.getChildren().add(canvas);
        setClip();
        createCanvasGrid(true);
    }

    public void createNodeRectangle(NodeDTO node) {
        Rectangle rect = ShapeHelper.createNodeRectangle(node);
        rootPane.getChildren().add(rect);
    }

    private void setClip() {
        Rectangle clip = new Rectangle(canvasWidth, canvasHeight);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        rootPane.setClip(clip);
    }

    private void createCanvasGrid(boolean sharp) {
        GraphicsContext gc = canvas.getGraphicsContext2D() ;
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
        for (int x = 0; x < canvas.getWidth(); x+=10) {
            double x1 ;
            if (sharp) {
                x1 = x + 0.5 ;
            } else {
                x1 = x ;
            }
            gc.moveTo(x1, 0);
            gc.lineTo(x1, canvas.getHeight());
            gc.stroke();
        }

        for (int y = 0; y < canvas.getHeight(); y+=10) {
            double y1 ;
            if (sharp) {
                y1 = y + 0.5 ;
            } else {
                y1 = y ;
            }
            gc.moveTo(0, y1);
            gc.lineTo(canvas.getWidth(), y1);
            gc.stroke();
        }
    }
}
