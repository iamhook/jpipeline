package com.jpipeline.entity.function;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterNode extends Node {

    private AtomicInteger counter = new AtomicInteger();

    public CounterNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        setStatus(new NodeStatus(String.valueOf(counter.get())));
    }

    @Override
    public void onInput(JPMessage message) {
        setStatus(new NodeStatus(String.valueOf(counter.incrementAndGet())));
        send(message);
    }

    @Override
    public void pressButton() {
        counter.set(0);
        setStatus(new NodeStatus(String.valueOf(counter.get())));
    }
}
