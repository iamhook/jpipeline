package com.jpipeline.jpipeline.rsocket;

import com.jpipeline.common.entity.Node;
import com.jpipeline.jpipeline.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Controller
public class RSocketController {

    private static final Logger log = LoggerFactory.getLogger(RSocketController.class);

    @Autowired
    private WorkflowService workflowService;

    @MessageMapping("/node/{nodeId}")
    public Flux<Node.NodeSignal> status(@DestinationVariable String nodeId) {
        Node node = workflowService.getNode(UUID.fromString(nodeId));
        return Flux.just(new Node.NodeSignal(Node.SignalType.STATUS, node.getStatus()))
                .concatWith(node.getSignalSink().asFlux());
    }

}
