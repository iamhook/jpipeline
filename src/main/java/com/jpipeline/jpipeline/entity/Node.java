package com.jpipeline.jpipeline.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Node {

    @Getter
    protected final UUID id;

    @Getter
    @Setter
    protected List<UUID> wires = new ArrayList<>();

    private List<Node> consumers = new ArrayList<>();

    protected Node(UUID id) {
        this.id = id;
    }

    public abstract void init();

    protected void send(Object message) {
        consumers.forEach(node -> node.onMessage(message));
    }

    public final void addConsumer(Node consumer) {
        consumers.add(consumer);
    }

    abstract void onMessage(Object message);
}
