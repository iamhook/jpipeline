package com.jpipeline.javafxclient.service;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import lombok.Getter;

public class ModelWorkflowService {

    @Getter
    private WorkflowConfig workflowConfig;

    public ModelWorkflowService(WorkflowConfig config) {
        this.workflowConfig = config;
    }

    public void createNode(NodeDTO node) {
        workflowConfig.addNode(node);
    }

    public void connectNodes(NodeDTO fromNode, NodeDTO toNode, int output) {
        fromNode.addWire(toNode.getId(), output);
    }

    public void deleteWire(NodeDTO fromNode, NodeDTO toNode, int output) {
        fromNode.deleteWire(toNode.getId(), output);
    }

    public void deleteNode(NodeDTO node) {
        workflowConfig.getNodes().remove(node);
        workflowConfig.getNodes().forEach(nodeDTO -> nodeDTO.getOutputs().forEach(wires -> wires.remove(node.getId())));
    }

    public NodeDTO getNode(String id) {
        return workflowConfig.getNodes().stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }
}
