package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;

public class RSocketService {

    RSocketStrategies strategies = RSocketStrategies.builder()
            .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
            .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
            .build();

    RSocketRequester requester = RSocketRequester.builder()
            .rsocketStrategies(strategies)
            .tcp("localhost", 7000);


    public <T> Flux<T> requestStream(Object object, String route, Class<T> clazz) throws JsonProcessingException {
        return requester.route(route).data(object).retrieveFlux(clazz);
    }
}
