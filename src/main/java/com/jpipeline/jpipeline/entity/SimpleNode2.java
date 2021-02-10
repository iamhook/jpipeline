package com.jpipeline.jpipeline.entity;

import java.util.UUID;

public class SimpleNode2 extends Node {

    public SimpleNode2(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        System.out.println("Hello, World! My name is " + this.getClass().getSimpleName());
    }

    @Override
    void onInput(Object message) {
        System.out.println(this.getClass().getSimpleName() + " received a message: " + message.toString());
    }

}
