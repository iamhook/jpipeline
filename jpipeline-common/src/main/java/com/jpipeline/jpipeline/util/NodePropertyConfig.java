package com.jpipeline.jpipeline.util;


import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class NodePropertyConfig {

    private static final Logger log = LoggerFactory.getLogger(NodePropertyConfig.class);

    private String name;

    private Object defaultValue;

    private SimpleType simpleType;

    private boolean required;

    public NodePropertyConfig(String name, Class clazz, String defaultValue, boolean required) {
        this.name = name;
        this.required = required;
        try {
            if (String.class.isAssignableFrom(clazz)) {
                simpleType = SimpleType.STRING;
                if (defaultValue != null)
                    this.defaultValue = defaultValue;
            } else if (Number.class.isAssignableFrom(clazz)) {
                simpleType = SimpleType.NUMBER;
                if (defaultValue != null)
                    this.defaultValue = clazz.getMethod("valueOf", String.class).invoke(null, defaultValue);
            } else if (Boolean.class.isAssignableFrom(clazz)) {
                simpleType = SimpleType.BOOLEAN;
                if (defaultValue != null)
                    this.defaultValue = Boolean.valueOf(defaultValue);
            } else {
                simpleType = SimpleType.OBJECT;
                // TODO analyze this
                this.defaultValue = defaultValue;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            simpleType = SimpleType.OBJECT;
        }

        return;
    }



    public enum SimpleType {
        NUMBER, STRING, BOOLEAN, OBJECT
    }

}
