package com.jpipeline.jpipeline.entity;

import java.util.UUID;

public class DebugNode extends Node {

    public DebugNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(Object message) {
        log.info(message.toString());
    }
}
