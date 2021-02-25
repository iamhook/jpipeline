package com.jpipeline.jpipeline.util;


import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class NodePropertyConfig<T> {

    // TODO add selectable values

    private static final Logger log = LoggerFactory.getLogger(NodePropertyConfig.class);

    private String name;

    private Object defaultValue;

    private SimpleType simpleType;

    private boolean required;

    public NodePropertyConfig(String name, Class<T> clazz, T defaultValue, boolean required) {
        this.name = name;
        this.required = required;
        try {
            if (String.class.isAssignableFrom(clazz)) {
                simpleType = SimpleType.STRING;
            } else if (Number.class.isAssignableFrom(clazz)) {
                simpleType = SimpleType.NUMBER;
            } else if (Boolean.class.isAssignableFrom(clazz)) {
                simpleType = SimpleType.BOOLEAN;
            } else {
                simpleType = SimpleType.OBJECT;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            simpleType = SimpleType.OBJECT;
        }

        this.defaultValue = defaultValue;

        return;
    }



    public enum SimpleType {
        NUMBER, STRING, BOOLEAN, OBJECT
    }

}
