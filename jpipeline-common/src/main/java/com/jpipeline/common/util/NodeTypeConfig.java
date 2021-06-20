package com.jpipeline.common.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NodeTypeConfig {
    private String name;
    private String category = "Other";
    private String color;
    private List<PropertyConfig> properties;
    private EditMode editMode = EditMode.SIMPLE;

    public PropertyConfig getPropertyConfig(String name) {
        return properties.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    private int inputs;
    private int outputs;
    private boolean hasButton;

    public boolean hasButton() {
        return hasButton;
    }

    public enum EditMode {
        NONE, SIMPLE, HTML_JAVASCRIPT, FXML_GROOVY
    }
}
