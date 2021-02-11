package com.jpipeline.jpipeline.entity;


import com.jpipeline.jpipeline.util.annotations.NodeButton;
import com.jpipeline.jpipeline.util.annotations.NodeProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class SimpleNode extends Node {

    @NodeProperty
    private String firstMessage;

    public SimpleNode(UUID id) {
        super(id);
    }

    @NodeButton
    public void pressButton() {
        send(firstMessage);
    }

    @Override
    public void onInit() {

        AtomicInteger i = new AtomicInteger();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            send(firstMessage + i.getAndIncrement());
        }, 0, 1000, TimeUnit.MILLISECONDS);

        System.out.println("Hello, World! My name is " + this.getClass().getSimpleName());
    }

    @Override
    void onInput(Object message) {
        System.out.println(this.getClass().getSimpleName() + " received a message: " + message.toString());
    }

}
