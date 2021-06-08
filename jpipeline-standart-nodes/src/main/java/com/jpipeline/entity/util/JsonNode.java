package com.jpipeline.entity.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class JsonNode extends Node {

    private static final ObjectMapper OM = new ObjectMapper();

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
                try {
                    payload = OM.readValue((String) value, CJson.class);
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }
            }
        } else if (action.equals("bidirectional") || action.equals("toString")) {
            payload = CJson.fromObject(value).toJson();
        }

        if (payload != null)
            send(message.setPayload(payload));
    }

}
