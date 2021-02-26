package com.jpipeline.jpipeline.entity;

import java.util.UUID;

public class InjectNode extends Node {

    public InjectNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(Object message) {

    }

    @Override
    public void pressButton() {
        send(firstMessage());
    }

    private String firstMessage() {
        return properties.getString("firstMessage");
    }

}
