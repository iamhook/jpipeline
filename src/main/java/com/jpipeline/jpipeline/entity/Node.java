package com.jpipeline.jpipeline.entity;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;

import java.util.*;

public abstract class Node {

    private static final Logger log = LoggerFactory.getLogger(Node.class);

    @Getter
    protected final UUID id;

    @Getter
    @Setter
    protected Set<UUID> wires = new HashSet<>();

    private Flux<Object> input;

    @Setter
    //private FluxSink output;;
    final Sinks.Many sink = Sinks.many().multicast().onBackpressureBuffer();

    protected Node(UUID id) {
        this.id = id;
    }

    public final void init() {
        if (input != null)
            input.subscribe(this::onInput);
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

    public void setInput(Flux<Object> input) {
        this.input = input;
        //input.subscribe(this::onInput);
    }

    public final void subscribe(Node subscriber) {
        subscriber.setInput(sink.asFlux());
    }

    abstract void onInput(Object message);

}
