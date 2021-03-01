package com.jpipeline.jpipeline.entity;


import com.jpipeline.jpipeline.util.annotations.NodeProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.UUID;

public class DelayNode extends Node {

    @NodeProperty
    private DelayProperty delay;

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
        int d = delay.getDelay();
        if ("s".equals(delay.getUnit())) {
            d *= 1000;
        } else if ("m".equals(delay.getUnit())) {
            d *= 60000;
        } else if ("h".equals(delay.getUnit())) {
            d *= 3600000;
        }
        subscriber.onSubscribe(sink.asFlux().delayElements(Duration.ofMillis(d)));
    }

    @Getter @Setter
    private static class DelayProperty {
        private int delay;
        private String unit;
    }

}
