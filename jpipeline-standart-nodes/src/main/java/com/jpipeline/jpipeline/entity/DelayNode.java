package com.jpipeline.jpipeline.entity;


import com.jpipeline.jpipeline.util.NodePropertyConfig;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DelayNode extends Node {

    public DelayNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(Object message) {
        send(message);
    }

    @Override
    public void subscribe(Node subscriber) {
        subscriber.onSubscribe(sink.asFlux().delayElements(Duration.ofMillis(delay())));
    }

    public static List<NodePropertyConfig> nodePropertyConfigs() {
        return Arrays.asList(
                new NodePropertyConfig("delay", Integer.class, 5000, true)
        );
    }

    private Integer delay() {
        return properties.getInteger("delay");
    }

}
