package com.jpipeline.common.entity;

import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JLogger;
import com.jpipeline.common.util.JPMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Node {

    @Getter
    protected final UUID id;

    @Getter
    private final String type;

    protected final JLogger log;

    @Getter @Setter
    private Boolean active = true;

    @Getter
    protected final CJson properties = new CJson();

    @Getter
    private NodeStatus status = null;

    @Getter
    protected final List<Sinks.Many> sinks = new ArrayList<>();

    @Getter
    private final Sinks.Many<NodeSignal> signalSink = Sinks.many().multicast().onBackpressureBuffer(Queues.XS_BUFFER_SIZE, false);

    public Node(UUID id) {
        this.id = id;
        this.type = this.getClass().getSimpleName();
        this.log = new JLogger(this);
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
                    log.error("Signal emission failed! signal {}, node {}", signal, this.id);
                }
            } else {
                log.debug(false,"Ignore signal because of no subscribers");
            }
        } else {
            log.error("Signal is null, node {}", this.id);
        }
    }

    public final void send(JPMessage message) {
        send(message, 0);
    }

    public final void send(JPMessage message, int output) {
        Sinks.Many sink = sinks.get(output);
        if (message != null) {
            if (sink.currentSubscriberCount() > 0) {
                Sinks.EmitResult result = sink.tryEmitNext(message);
                if (result.isFailure()) {
                    log.error("Message emission failed! message {}, node {}", message, this.id);
                }
            } else {
                log.debug(false, "Ignore signal because of no subscribers. message {}, node {}", message, this.id);
            }
        } else {
            log.error("Message is null, node {}", this.id);
        }
    }

    public void onSubscribe(Flux<JPMessage> input) {
        if (input != null)
            input.subscribe(this::onInputHandler);
    }

    public void subscribe(Node subscriber) {
        subscribe(subscriber, 0);
    }

    public void subscribe(Node subscriber, int output) {
        subscriber.onSubscribe(sinks.get(output).asFlux());
    }

    private void onInputHandler(JPMessage message) {
        try {
            onInput(new JPMessage(message));
        } catch (Exception e) {
           log.error(e.toString(), e);
        }
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
        STATUS, DEBUG, ERROR
    }
}
