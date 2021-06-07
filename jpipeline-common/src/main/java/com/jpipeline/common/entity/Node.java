package com.jpipeline.common.entity;

import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JPMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

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

    @Getter
    private NodeStatus status = null;

    protected final Sinks.Many sink = Sinks.many().multicast().onBackpressureBuffer();

    @Getter
    private final Sinks.Many<NodeSignal> signalSink = Sinks.many().multicast().onBackpressureBuffer(Queues.XS_BUFFER_SIZE, false);

    public Node(UUID id) {
        this.id = id;
        this.type = this.getClass().getSimpleName();
    }

    public final void init() {
        onInit();
    }

    public abstract void onInit();

    protected final void setStatus(NodeStatus status) {
        this.status = status;
        sendSignal(new NodeSignal(SignalType.STATUS, status, this.id));
    }

    public void sendSignal(NodeSignal signal) {
        if (signal != null) {
            if (signalSink.currentSubscriberCount() > 0) {
                Sinks.EmitResult result = signalSink.tryEmitNext(signal);
                if (result.isFailure()) {
                    log.error("Emission failed! signal {}, node {}", signal, this.id);
                }
            } else {
                log.debug("Ignore signal because of no subscribers");
            }
        } else {
            log.error("Signal is null, node {}", this.id);
        }
    }

    public final void send(JPMessage message) {
        if (message != null) {
            if (sink.currentSubscriberCount() > 0) {
                Sinks.EmitResult result = sink.tryEmitNext(message);
                if (result.isFailure()) {
                    log.error("Emission failed! message {}, node {}", message, this.id);
                }
            } else {
                log.debug("Ignore signal because of no subscribers. message {}, node {}", message, this.id);
            }
        } else {
            log.error("Message is null, node {}", this.id);
        }
    }

    public void onSubscribe(Flux<JPMessage> input) {
        if (input != null)
            input.subscribe(this::onInput);
    }

    public void subscribe(Node subscriber) {
        subscriber.onSubscribe(sink.asFlux());
    }

    public abstract void onInput(JPMessage message);

    public void pressButton() {};

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class NodeStatus {
        String status;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class NodeSignal {
        private SignalType type;
        private Object body;
        private UUID nodeId;
    }

    public enum SignalType {
        STATUS, DEBUG
    }
}
