package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import lombok.Getter;

public class ModelWorkflowService implements IWorkflowService {

    @Getter
    private WorkflowConfig workflowConfig;
    private WorkflowService workflowService;

    public ModelWorkflowService(WorkflowConfig config, WorkflowService workflowService) {
        this.workflowConfig = config;
        this.workflowService = workflowService;
    }

    @Override
    public void createNode(NodeDTO node) {
        workflowConfig.addNode(node);
    }

    @Override
    public void connectNodes(NodeDTO fromNode, NodeDTO toNode) {
        fromNode.addWire(toNode.getId());
    }

    @Override
    public void disconnectNodes(NodeDTO fromNode, NodeDTO toNode) {
        fromNode.deleteWire(toNode.getId());
    }

    @Override
    public void deleteNode(NodeDTO node) {
        workflowConfig.getNodes().remove(node);
        workflowConfig.getNodes().forEach(nodeDTO -> nodeDTO.deleteWire(node.getId()));
    }

    public NodeDTO getNode(String id) {
        return workflowConfig.getNodes().stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }
}
