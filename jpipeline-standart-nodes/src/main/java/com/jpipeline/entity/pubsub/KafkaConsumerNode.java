package com.jpipeline.entity.pubsub;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KafkaConsumerNode extends Node {

    @NodeProperty
    private String servers;

    @NodeProperty
    private String topic;

    @NodeProperty
    private String offset;

    public KafkaConsumerNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        KafkaReceiver<Integer, String> kafkaReceiver = KafkaReceiver.create(buildReceiverOptions());

        Flux<ReceiverRecord<Integer, String>> inboundFlux = kafkaReceiver.receive();

        inboundFlux.subscribe(r -> {
            send(new JPMessage(r.value()));
            r.receiverOffset().acknowledge();
        });
    }

    private ReceiverOptions<Integer, String> buildReceiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.getId().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        /*if(offset == Offset.START) {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        } else if(offset == Offset.END) {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        }*/

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);

        ReceiverOptions receiverOptions = ReceiverOptions.create(props);
        return receiverOptions.subscription(Collections.singleton(topic));
    }
}
