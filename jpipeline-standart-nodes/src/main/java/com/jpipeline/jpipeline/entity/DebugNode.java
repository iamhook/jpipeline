package com.jpipeline.jpipeline.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DebugNode extends Node {

    private static final Logger log = LoggerFactory.getLogger(Node.class);

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
