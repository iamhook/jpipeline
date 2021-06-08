package com.jpipeline.entity.common;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;

import java.util.UUID;

public class DebugNode extends Node {

    public DebugNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {
        log.debug(message);
    }
}
