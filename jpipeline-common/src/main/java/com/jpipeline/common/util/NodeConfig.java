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

    private int inputs;
    private int outputs;
    private boolean hasButton;

    public boolean hasButton() {
        return hasButton;
    }
}
