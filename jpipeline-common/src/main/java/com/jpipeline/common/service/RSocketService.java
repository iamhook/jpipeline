package com.jpipeline.common.service;

import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;

import java.net.URI;

public class RSocketService {

    RSocketStrategies strategies;
    RSocketRequester requester;

    public RSocketService(String host) {
        strategies = RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
                .build();

        requester = RSocketRequester.builder()
                .rsocketStrategies(strategies)
                .websocket(URI.create(host));
    }

    public <T> Flux<T> requestStream(Object object, String route, Class<T> clazz) {
        return requester.route(route).data(object).retrieveFlux(clazz);
    }
}
