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
    private List<Set<String>> outputs;
    private String color;
    private Double x;
    private Double y;
    private boolean hasButton;
    private boolean hasInput;

    public Boolean hasButton() {
        return hasButton;
    }

    public Boolean hasInput() {
        return hasInput;
    }

    public NodeDTO() {}

    public void addWire(String wire, int output) {
        outputs.get(output).add(wire);
    }
    public void deleteWire(String wire, int output) {
        outputs.get(output).remove(wire);
    }
}
