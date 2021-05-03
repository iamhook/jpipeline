package com.jpipeline.javafxclient.ui.elements;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;
import javafx.scene.layout.Pane;

public class WorkflowService implements IWorkflowService {

    private ViewWorkflowService viewService;
    private ModelWorkflowService modelService;

    @Override
    public void createNode(NodeDTO node) {
        modelService.createNode(node);
        viewService.createNode(node);
    }

    @Override
    public void connectNodes(NodeDTO fromNode, NodeDTO toNode) {
        modelService.connectNodes(fromNode, toNode);
        viewService.connectNodes(fromNode, toNode);
    }

    @Override
    public void disconnectNodes(NodeDTO fromNode, NodeDTO toNode) {
        modelService.disconnectNodes(fromNode, toNode);
        viewService.disconnectNodes(fromNode, toNode);
    }

    @Override
    public void deleteNode(NodeDTO node) {
        modelService.deleteNode(node);
        viewService.deleteNode(node);
    }

    public WorkflowService(WorkflowConfig workflowConfig, Pane canvasPane) {
        this.viewService = new ViewWorkflowService(canvasPane, this);
        this.modelService = new ModelWorkflowService(workflowConfig, this);

        workflowConfig.getNodes().forEach(viewService::createNode);
        workflowConfig.getNodes().forEach(node -> {
                    for (String wire : node.getWires()) {
                        NodeDTO toNode = modelService.getNode(wire);
                        viewService.connectNodes(node, toNode);
                    }
                });
    }

    public void createNode(String nodeType) {
        NodeDTO node = NodeService.createNewNode(nodeType);
        createNode(node);
    }

    public void deploy() {
        ManagerService.deploy(modelService.getWorkflowConfig());
    }

    public void destroy() {
        viewService.destroy();
    }

}
