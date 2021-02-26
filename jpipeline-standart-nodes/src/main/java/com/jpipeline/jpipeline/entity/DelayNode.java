package com.jpipeline.jpipeline.entity;


import java.time.Duration;
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

    private Integer delay() {
        return properties.getInteger("delay");
    }

}
