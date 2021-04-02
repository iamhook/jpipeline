package com.jpipeline.jpipeline.entity;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.util.UUID;

public class InjectNode extends Node {

    @NodeProperty
    private String firstMessage;

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
        send(firstMessage);
    }


}
