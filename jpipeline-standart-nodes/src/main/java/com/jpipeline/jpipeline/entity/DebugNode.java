package com.jpipeline.jpipeline.entity;

import com.jpipeline.common.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DebugNode extends Node {

    private static final Logger debugLog = LoggerFactory.getLogger(Node.class);

    public DebugNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(Object message) {
        debugLog.info(message.toString());
    }
}
