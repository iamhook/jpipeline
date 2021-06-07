package com.jpipeline.common.util;

import java.util.Map;

public class JPMessage extends CJson {

    private static final String p_key = "payload";

    public JPMessage(Map map) {
        super(map);
    }

    public JPMessage(Object payload) {
        put(p_key, payload);
    }

    public JPMessage setPayload(Object payload) {
        put(p_key, payload);
        return this;
    }

    public Object getPayload() {
        return get(p_key);
    }

}
