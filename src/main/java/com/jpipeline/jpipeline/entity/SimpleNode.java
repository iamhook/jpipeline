package com.jpipeline.jpipeline.entity;


import com.jpipeline.jpipeline.util.annotations.NodeProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SimpleNode extends Node {

    @NodeProperty
    private String firstMessage;

    public SimpleNode(UUID id) {
        super(id);
    }

    public void pressButton() {
        send(firstMessage);
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
