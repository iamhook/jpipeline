package com.jpipeline.common;

import com.jpipeline.common.dto.NodeDTO;
import lombok.*;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WorkflowConfig {

    private Collection<NodeDTO> nodes;

    public void addNode(NodeDTO node) {
        nodes.add(node);
    }

}
