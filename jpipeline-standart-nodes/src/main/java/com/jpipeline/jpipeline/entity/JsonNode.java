package com.jpipeline.jpipeline.entity;


import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.util.UUID;

public class JsonNode extends Node {

    @NodeProperty
    private String action;

    @NodeProperty
    private String field;

    public JsonNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {
        Object value = message.get(field);
        Object payload = null;

        if (value instanceof String) {
            if ((action.equals("bidirectional") || action.equals("toObject"))) {
                payload = CJson.fromJson((String) value);
            }
        } else if (action.equals("bidirectional") || action.equals("toString")) {
            payload = CJson.fromObject(value).toJson();
        }

        if (payload != null)
            send(message.setPayload(payload));
    }

}
