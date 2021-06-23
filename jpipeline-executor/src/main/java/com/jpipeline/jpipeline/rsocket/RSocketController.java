package com.jpipeline.jpipeline.rsocket;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jpipeline.common.entity.Node;
import com.jpipeline.jpipeline.service.WorkflowService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collection;

@Controller
public class RSocketController {

    private static final Logger log = LoggerFactory.getLogger(RSocketController.class);

    @Autowired
    private WorkflowService workflowService;

    @Setter
    private String jwtSecret;

    private void checkAuthToken(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Auth token not found");
        }

        DecodedJWT verifiedToken = JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                .build()
                .verify(authToken);

        if (!verifiedToken.getClaim("token").asString().equals(jwtSecret))
            throw new RuntimeException("Auth failed");
    }

    @MessageMapping("/status")
    public Flux<Node.NodeSignal> status(String authToken) {
        checkAuthToken(authToken);
        Collection<Node> nodes = workflowService.getNodes();

        return Flux.fromIterable(nodes)
                .filter(node -> node.getStatus() != null)
                .map(node -> new Node.NodeSignal(Node.SignalType.STATUS, node.getStatus(), node.getId()))
                .concatWith(Flux.fromIterable(nodes)
                        .flatMap(node -> node.getSignalSink().asFlux())
                        .filter(nodeSignal -> nodeSignal.getType().equals(Node.SignalType.STATUS))
                        .onBackpressureDrop()
                        .limitRate(1)
                        .delayElements(Duration.ofMillis(100)));
    }
    @MessageMapping("/debug")
    public Flux<Node.NodeSignal> debug(String authToken) {
        checkAuthToken(authToken);
        Collection<Node> nodes = workflowService.getNodes();

        return Flux.fromIterable(nodes)
                .flatMap(node -> node.getSignalSink().asFlux())
                .filter(nodeSignal -> nodeSignal.getType().equals(Node.SignalType.DEBUG))
                .onBackpressureDrop()
                .limitRate(1)
                .delayElements(Duration.ofMillis(50));
    }

    @MessageMapping("/errors")
    public Flux<Node.NodeSignal> errors(String authToken) {
        checkAuthToken(authToken);
        Collection<Node> nodes = workflowService.getNodes();

        return Flux.fromIterable(nodes)
                .flatMap(node -> node.getSignalSink().asFlux())
                .filter(nodeSignal -> nodeSignal.getType().equals(Node.SignalType.ERROR))
                .onBackpressureDrop()
                .limitRate(1)
                .delayElements(Duration.ofMillis(50));
    }

}
