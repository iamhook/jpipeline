package com.jpipeline.jpipeline.entity;


import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.time.Duration;
import java.util.UUID;

public class DelayNode extends Node {

    //private DelayProperty delay;

    @NodeProperty
    private int delay;

    @NodeProperty
    private String unit;

    private Integer counter = 0;

    public DelayNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {
        send(message);
    }

    @Override
    public void subscribe(Node subscriber) {
        int d = delay;
        if ("s".equals(unit)) {
            d *= 1000;
        } else if ("m".equals(unit)) {
            d *= 60000;
        } else if ("h".equals(unit)) {
            d *= 3600000;
        }
        subscriber.onSubscribe(sink.asFlux().delayElements(Duration.ofMillis(d)).doOnNext(o -> setStatus(new NodeStatus((counter++).toString()))));
    }

    /*@Getter @Setter
    private static class DelayProperty {
        private int delay;
        private String unit;
        public DelayProperty() {}
    }*/

}
