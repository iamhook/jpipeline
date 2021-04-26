package com.jpipeline.common.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NodeConfig {
    private String color;
    private List<PropertyConfig> properties;
}
