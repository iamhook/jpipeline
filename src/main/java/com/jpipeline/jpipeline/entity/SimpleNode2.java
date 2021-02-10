package com.jpipeline.jpipeline.entity;

import java.util.UUID;

public class SimpleNode2 extends Node {

    public SimpleNode2(UUID id) {
        super(id);
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
        return SimpleNode2.class.getName();
    }

    public static String getShortType() {
        return SimpleNode2.class.getSimpleName();
    }

}
