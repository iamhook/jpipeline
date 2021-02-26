package com.jpipeline.jpipeline.dto;

import com.jpipeline.jpipeline.util.CJson;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class NodeDTO {

    private String id;
    private String type;
    private Boolean active;
    private CJson properties;
    private List<String> wires;

    public NodeDTO() {
    }

    public NodeDTO(String id, String type, Boolean active, CJson properties, List<String> wires) {
        this.id = id;
        this.type = type;
        this.active = active;
        this.properties = properties;
        this.wires = wires;
    }
}
