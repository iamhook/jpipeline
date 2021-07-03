package com.jpipeline.entity.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KafkaProducerNode extends Node {

    private static final ObjectMapper OM = new ObjectMapper();

    @NodeProperty
    private String servers;

    @NodeProperty
    private String topic;

    private KafkaSender<String, String> sender;

    public KafkaProducerNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        this.sender = KafkaSender.create(buildSenderOptions());
    }

    @Override
    public void onInput(JPMessage message) {
        Object payload = message.getPayload();

        String messageToSend;

        if (payload instanceof String) {
            messageToSend = (String) payload;
        } else if (payload instanceof Number) {
            messageToSend = payload.toString();
        } else if (payload instanceof Map) {
            messageToSend = new CJson((Map) payload).toJson();
        } else {
            try {
                messageToSend = OM.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                log.error(e.toString(), e);
                return;
            }
        }

        sender.send(Mono.just(SenderRecord.create(
                new ProducerRecord<>(topic, messageToSend), null)))
                .doOnError(e -> log.error("Send failed, msg " + message, e))
                .subscribe();
    }

    private SenderOptions<String, String> buildSenderOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, this.getId().toString());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return SenderOptions.create(props);
    }
}
