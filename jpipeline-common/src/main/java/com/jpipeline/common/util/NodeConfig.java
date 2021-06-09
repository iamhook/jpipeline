package com.jpipeline.common.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NodeConfig {
    private String name;
    private String category = "Other";
    private String color;
    private List<PropertyConfig> properties;

    public PropertyConfig getPropertyConfig(String name) {
        return properties.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    private int inputs;
    private int outputs;
    private boolean hasButton;

    public boolean hasButton() {
        return hasButton;
    }
}
