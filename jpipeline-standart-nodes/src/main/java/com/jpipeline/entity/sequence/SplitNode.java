package com.jpipeline.entity.sequence;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

public class SplitNode extends Node {

    // STRINGS

    @NodeProperty
    private String stringDelimiter;

    // LISTS

    @NodeProperty
    private Integer subListSize;

    // OBJECTS

    @NodeProperty
    private String copyKeyInto;

    public SplitNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {
        Object payload = message.getPayload();

        if (payload instanceof String) {

            String payloadString = (String) payload;
            Arrays.stream(payloadString.split(stringDelimiter)).forEach(s -> send(new JPMessage(s)));
        } else if (payload instanceof Collection) {
            List list = new ArrayList((Collection) payload);

            int size = list.size();
            int parts = (int) Math.ceil((double)size/subListSize);
            IntStream.range(0, parts)
                            .mapToObj(i -> {
                                if (subListSize == 1) {
                                    return list.get(i);
                                } else {
                                    return list.subList(i * subListSize, Math.min((i + 1) * subListSize, list.size()));
                                }
                            })
                    .forEach(l -> send(new JPMessage(l)));
        } else if (payload instanceof Map) {
            Map<Object, Object> map = (Map) payload;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                JPMessage msg = new JPMessage(entry.getValue());
                if (copyKeyInto != null && !copyKeyInto.isEmpty()) {
                    msg.put(copyKeyInto, entry.getKey());
                }
            }
        }
    }
}
