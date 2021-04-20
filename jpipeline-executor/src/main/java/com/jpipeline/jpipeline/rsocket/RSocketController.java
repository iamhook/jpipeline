package com.jpipeline.jpipeline.rsocket;

import com.jpipeline.common.entity.Node;
import com.jpipeline.jpipeline.service.WorkflowService;
import io.rsocket.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class RSocketController {

    @Autowired
    private WorkflowService workflowService;

    /*Sinks.Many sink = Sinks.many().multicast().onBackpressureBuffer();

    @PostConstruct
    public void init() {
        Executors.newSingleThreadExecutor().submit(() -> {
            AtomicInteger i = new AtomicInteger();

            while (true) {
                sink.tryEmitNext("emit " + i.getAndIncrement());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

    }*/

    @MessageMapping("")
    public Flux test(Mono<Payload> payload) {
        return Flux.just("1", "2", "3");
    }

    @MessageMapping("/node/{nodeId}")
    public Flux<Node.NodeSignal> status(@DestinationVariable String nodeId) {
        Node node = workflowService.getNode(UUID.fromString(nodeId));
        return Flux.just(
                new Node.NodeSignal(Node.SignalType.STATUS, node.getStatus())
        ).concatWith(node.getSignalSink().asFlux());
    }

}
