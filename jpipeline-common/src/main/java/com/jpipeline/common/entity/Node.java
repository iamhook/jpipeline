package com.jpipeline.common.entity;

import com.jpipeline.common.util.CJson;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

public abstract class Node {

    protected static final Logger log = LoggerFactory.getLogger(Node.class);

    @Getter
    protected final UUID id;

    @Getter
    private final String type;

    @Getter @Setter
    private Boolean active = true;

    @Getter
    protected final CJson properties = new CJson();

    protected final Sinks.Many sink = Sinks.many().multicast().onBackpressureBuffer();

    public Node(UUID id) {
        this.id = id;
        this.type = this.getClass().getSimpleName();
    }

    public final void init() {
        onInit();
    }

    public abstract void onInit();

    public final void send(Object message) {
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

    public void subscribe(Node subscriber) {
        subscriber.onSubscribe(sink.asFlux());
    }

    public abstract void onInput(Object message);

    public void pressButton() {};
}
