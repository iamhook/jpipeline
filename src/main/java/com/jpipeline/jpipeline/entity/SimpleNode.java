package com.jpipeline.jpipeline.entity;



import com.jpipeline.jpipeline.util.annotations.NodeProperty;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleNode extends Node {

    @NodeProperty
    @Setter
    private String firstMessage;

    public SimpleNode(UUID id) {
        super(id);
    }

    public void pressButton() {
        send(firstMessage);
    }

    @Override
    public void onInit() {
        System.out.println("Hello, World! My name is " + getShortType());
    }

    @Override
    void onInput(Object message) {
        System.out.println(getShortType() + " received a message: " + message.toString());
    }

    public static String getType() {
        return SimpleNode.class.getName();
    }

    public static String getShortType() {
        return SimpleNode.class.getSimpleName();
    }

}
