package com.jpipeline.common.dto;

import com.jpipeline.common.util.CJson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class NodeDTO {

    private String id;
    private String type;
    private Boolean active;
    private CJson properties;
    private Set<String> wires;
    private String color;
    private Double x;
    private Double y;

    public NodeDTO() {}

    public void addWire(String wire) {
        wires.add(wire);
    }
    public void deleteWire(String wire) {
        wires.remove(wire);
    }
}
