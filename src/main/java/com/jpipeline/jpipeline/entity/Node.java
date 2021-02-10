package com.jpipeline.jpipeline.entity;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class Node {

    private static final Logger log = LoggerFactory.getLogger(Node.class);

    @Getter
    protected final UUID id;

    @Getter
    private final String type;

    @Getter
    @Setter
    protected Set<UUID> wires = new HashSet<>();

    final Sinks.Many sink = Sinks.many().multicast().onBackpressureBuffer();

    protected Node(UUID id) {
        this.id = id;
        this.type = this.getClass().getSimpleName();
    }

    public final void init() {
        onInit();
    }

    protected abstract void onInit();

    protected void send(Object message) {
        if (message != null) {
            Sinks.EmitResult result = sink.tryEmitNext(message);
            if (result.isFailure()) {
                log.error("Emission failed! message {}, node {}", message, this.id);
            }
        } else {
            log.error("Message is null, node {}", this.id);
        }
    }

    public void onSubscribe(Flux<Object> input) {
        if (input != null)
            input.subscribe(this::onInput);
    }

    public final void subscribe(Node subscriber) {
        subscriber.onSubscribe(sink.asFlux());
    }

    abstract void onInput(Object message);
}
