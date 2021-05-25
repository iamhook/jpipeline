package com.jpipeline.jpipeline.rsocket;

import com.jpipeline.common.entity.Node;
import com.jpipeline.jpipeline.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Controller
public class RSocketController {

    private static final Logger log = LoggerFactory.getLogger(RSocketController.class);

    @Autowired
    private WorkflowService workflowService;

    @MessageMapping("/node")
    public Flux<Node.NodeSignal> status() {
        Collection<Node> nodes = workflowService.getNodes();

        return Flux.fromIterable(nodes)
                .filter(node -> node.getStatus() != null)
                .map(node -> new Node.NodeSignal(Node.SignalType.STATUS, node.getStatus(), node.getId()))
                .concatWith(Flux.fromIterable(nodes).flatMap(node -> node.getSignalSink().asFlux()));
    }

}
