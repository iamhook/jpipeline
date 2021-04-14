package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.dto.NodeDTO;

public interface IWorkflowService {
    
    void createNode(NodeDTO node);

    void connectNodes(NodeDTO fromNode, NodeDTO toNode);

    void disconnectNodes(NodeDTO fromNode, NodeDTO toNode);

    void deleteNode(NodeDTO node);

}
