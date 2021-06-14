package com.jpipeline.common;

import com.jpipeline.common.dto.NodeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowConfig {

    private Collection<NodeDTO> nodes;

    public void addNode(NodeDTO node) {
        nodes.add(node);
    }

}
