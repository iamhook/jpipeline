package com.jpipeline.jpipeline.entity;

import com.jpipeline.jpipeline.util.NodePropertyConfig;

import java.util.Arrays;
import java.util.List;
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

    public static List<NodePropertyConfig> nodePropertyConfigs() {
        return Arrays.asList(
                new NodePropertyConfig("firstMessage", String.class, null, true)
        );
    }

    private String firstMessage() {
        return properties.getString("firstMessage");
    }

}
