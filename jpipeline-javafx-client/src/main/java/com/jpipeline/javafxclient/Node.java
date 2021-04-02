package com.jpipeline.javafxclient;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Node {

    private static GraphicsContext gc;
    private static int interval;
    private static int lineWidth;
    private static int nodeWidth;

    public final int x;
    public final int y;
    private boolean changed = true;
    private NodeType type = NodeType.DEFAULT;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public void draw() {
        // если узел не менял свой тип - нет смысла его перерисовывать
        if (!changed) return;

        // стираем прямоугольник из-за проблемы с тонкими линиями
        gc.setFill(Color.WHITE);
        gc.fillRect(x * nodeWidth + 2 * interval * (x + 1), y * nodeWidth + 2 * interval * (y + 1), nodeWidth, nodeWidth);

        // выбираем цвет в зависимости от типа узла
        switch (type) {
            case DEFAULT:
                gc.setFill(Color.WHITE); break;
            case BARRIER:
                gc.setFill(Color.BLACK); break;
            case SOURCE:
                gc.setFill(Color.GREEN); break;
            case DESTINATION:
                gc.setFill(Color.RED); break;
            case MARKED:
                gc.setFill(Color.LIGHTGRAY); break;
            case INSPECTED:
                gc.setFill(Color.GRAY); break;
            case TRACED:
                gc.setFill(Color.ORANGE); break;
        }

        // рисуем границу
        gc.strokeRect(x * nodeWidth + 2 * interval * (x + 1) + lineWidth,
                y * nodeWidth + 2 * interval * (y + 1) + lineWidth,
                nodeWidth - 2 * lineWidth,
                nodeWidth - 2 * lineWidth);
        // рисуем заливку
        gc.fillRect(x * nodeWidth + 2 * interval * (x + 1) + lineWidth,
                y * nodeWidth + 2 * interval * (y + 1) + lineWidth,
                nodeWidth - 2 * lineWidth,
                nodeWidth - 2 * lineWidth);
        // теперь узел перерисован
        changed = false;
    }


    public void setType(NodeType type) {
        if (this.type != NodeType.SOURCE && this.type != NodeType.DESTINATION) {
            this.type = type;
            changed = true;
        }
    }

    public NodeType getType() {
        return type;
    }

    public void forceType(NodeType type) {
        this.type = type;
        changed = true;
    }

    public boolean isBarrier() { return type == NodeType.BARRIER; }
    public boolean isSource() { return type == NodeType.SOURCE; }
    public boolean isDestination() { return type == NodeType.DESTINATION; }
    public boolean isMarked() { return type == NodeType.MARKED; }

    public static void setGc(GraphicsContext gc) {
        Node.gc = gc;
    }

    public static void setInterval(int interval) {
        Node.interval = interval;
    }

    public static void setNodeWidth(int nodeWidth) {
        Node.nodeWidth = nodeWidth;
    }

    public static void setLineWidth(int lineWidth) {
        Node.lineWidth = lineWidth;
    }
}
