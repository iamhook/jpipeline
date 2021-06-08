package com.jpipeline.entity.common;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.util.UUID;

public class InjectNode extends Node {

    @NodeProperty
    private String payload;

    public InjectNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {

    }

    @Override
    public void pressButton() {
        send(new JPMessage(payload));
    }


}
