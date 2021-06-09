package com.jpipeline.entity;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;

import java.util.UUID;

public class SwitchNode extends Node {


    public SwitchNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {

    }
}
