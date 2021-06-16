package com.jpipeline.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jpipeline.common.util.CJson;
import lombok.*;

import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class NodeDTO {

    private String id;
    private String type;
    private Boolean active;
    private CJson properties;
    private List<Set<String>> outputs;

    @EqualsAndHashCode.Exclude
    private String color;

    @EqualsAndHashCode.Exclude
    private Double x;

    @EqualsAndHashCode.Exclude
    private Double y;

    private boolean hasButton;
    private boolean hasInput;

    @JsonIgnore @EqualsAndHashCode.Exclude
    private Runnable modelChangedCallback;

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

    public void modelChanged() {
        if (modelChangedCallback != null)
            modelChangedCallback.run();
    }
}
