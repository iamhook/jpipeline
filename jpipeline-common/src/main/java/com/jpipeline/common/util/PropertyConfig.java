package com.jpipeline.common.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class PropertyConfig {

    private String name;
    private Type type;
    private Object defaultValue;
    private boolean required;
    private boolean multiple;
    private List<PropertyConfig> nested;
    private List<Object> variants;

    @JsonIgnore
    public boolean isComplex() {
        return Type.COMPLEX.equals(type);
    }

    @JsonIgnore
    public boolean isString() {
        return Type.STRING.equals(type);
    }

    @JsonIgnore
    public boolean isNumber() {
        return Type.NUMBER.equals(type);
    }

    public Object getDefaultValue() {
        if (isComplex()) {
            CJson defaultValue = new CJson();
            for (PropertyConfig config : nested) {
                defaultValue.put(config.getName(), config.getDefaultValue());
            }
            return defaultValue;
        } else {
            return defaultValue;
        }
    }

    private enum Type {
        NUMBER, STRING, COMPLEX
    }

}