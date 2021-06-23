package com.jpipeline.manager.rsocket;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.service.RSocketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;

@Controller
public class RSocketController {

    @Value("${jpipeline.executor.rsocket-port}")
    private Integer executorRSocketPort;

    private RSocketService rSocketService;

    @PostConstruct
    private void init() {
        rSocketService = new RSocketService("ws://localhost:" + executorRSocketPort);
    }

    @MessageMapping("/status")
    public Flux<Node.NodeSignal> status(String authToken) {
        return rSocketService.requestStream(authToken, "/status", Node.NodeSignal.class);
    }
    @MessageMapping("/debug")
    public Flux<Node.NodeSignal> debug(String authToken) {
        return rSocketService.requestStream(authToken, "/debug", Node.NodeSignal.class);
    }
    @MessageMapping("/errors")
    public Flux<Node.NodeSignal> error(String authToken) {
        return rSocketService.requestStream(authToken, "/errors", Node.NodeSignal.class);
    }

}
