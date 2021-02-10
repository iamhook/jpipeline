package com.jpipeline.jpipeline.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class Node {

    @Getter
    protected final UUID id;

    @Getter
    @Setter
    protected Set<UUID> wires = new HashSet<>();

    private Set<Node> consumers = new HashSet<>();

    protected Node(UUID id) {
        this.id = id;
    }

    public abstract void init();

    protected void send(Object message) {
        consumers.forEach(node -> node.onInput(message));
    }

    public final void addConsumer(Node consumer) {
        consumers.add(consumer);
    }

    abstract void onInput(Object message);

}
