package com.jpipeline.common.util;

public class JPMessage extends CJson {

    private static final String p_key = "payload";

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
