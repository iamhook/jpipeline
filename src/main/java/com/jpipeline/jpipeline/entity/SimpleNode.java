package com.jpipeline.jpipeline.entity;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleNode extends Node {

    public SimpleNode(UUID id) {
        super(id);
    }

    public void pressButton() {
        send("Hello from " + getShortType());
    }

    @Override
    public void init() {
        System.out.println("Hello, World! My name is " + getShortType());

        //ExecutorService executorService = Executors.newSingleThreadExecutor();
        //executorService.exe
    }

    @Override
    void onMessage(Object message) {
        System.out.println(getShortType() + " received a message: " + message.toString());
    }

    public static String getType() {
        return SimpleNode.class.getName();
    }

    public static String getShortType() {
        return SimpleNode.class.getSimpleName();
    }

}
