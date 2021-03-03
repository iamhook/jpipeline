package com.jpipeline.jpipeline;

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

    public boolean isComplex() {
        return Type.COMPLEX.equals(type);
    }
    public boolean isString() {
        return Type.STRING.equals(type);
    }

    private enum Type {
        NUMBER, STRING, COMPLEX
    }

}
