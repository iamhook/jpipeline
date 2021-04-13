package com.jpipeline.common;

import com.jpipeline.common.dto.NodeDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WorkflowConfig {

    private List<NodeDTO> nodes;

    public void addNode(NodeDTO node) {
        nodes.add(node);
    }

}
