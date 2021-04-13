package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;

public class WorkflowContextHolder {

    private WorkflowConfig workflowConfig;

    private CanvasContext canvasContext;

    public WorkflowContextHolder(WorkflowConfig workflowConfig, CanvasContext canvasContext) {
        this.workflowConfig = workflowConfig;
        this.canvasContext = canvasContext;

        for (NodeDTO node : workflowConfig.getNodes()) {
            this.canvasContext.createNodeRectangle(node);
        }
        for (NodeDTO node : workflowConfig.getNodes()) {
            for (String wire : node.getWires()) {
                NodeDTO toNode = workflowConfig.getNodes().stream()
                        .filter(n -> n.getId().equals(wire)).findFirst().orElse(null);
                this.canvasContext.connectNodes(node, toNode);
            }
        }
    }

    public void createNode(String nodeType) {
        NodeDTO node = NodeService.createNewNode(nodeType);
        createNode(node);
    }

    private void createNode(NodeDTO node) {
        workflowConfig.addNode(node);
        canvasContext.createNodeRectangle(node);
    }

    public void deploy() {
        ManagerService.deploy(workflowConfig);
    }

}
