package com.jpipeline.javafxclient.ui;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.controller.MainMenuController;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;
import javafx.scene.layout.Pane;
import lombok.Setter;

import java.util.Set;

public class WorkflowService {

    private ViewWorkflowService viewService;
    private ModelWorkflowService modelService;

    @Setter
    private MainMenuController controller;

    public void createNode(NodeDTO node) {
        modelService.createNode(node);
        viewService.createNode(node, false);
    }

    public void connectNodes(NodeDTO fromNode, NodeDTO toNode, int output) {
        modelService.connectNodes(fromNode, toNode, output);
        viewService.connectNodes(fromNode, toNode, output);
    }

    public void disconnectNodes(NodeDTO fromNode, NodeDTO toNode, int output) {
        modelService.disconnectNodes(fromNode, toNode, output);
        viewService.disconnectNodes(fromNode, toNode);
    }

    public void deleteNode(NodeDTO node) {
        modelService.deleteNode(node);
        viewService.deleteNode(node);
    }

    public WorkflowService(WorkflowConfig workflowConfig, Pane canvasPane) {
        this.viewService = new ViewWorkflowService(canvasPane, this);
        this.modelService = new ModelWorkflowService(workflowConfig, this);

        workflowConfig.getNodes().forEach(node -> viewService.createNode(node, true));
        workflowConfig.getNodes().forEach(node -> {
                    int i = 0;
                    for (Set<String> wires : node.getOutputs()) {
                        for (String wire : wires) {
                            NodeDTO toNode = modelService.getNode(wire);
                            viewService.connectNodes(node, toNode, i++);
                        }
                    }
                });
    }

    public void createNode(String nodeType) {
        NodeDTO node = NodeService.createNewNode(nodeType);
        createNode(node);
    }

    public void deploy() {
        ManagerService.deploy(modelService.getWorkflowConfig());
        controller.onDeploy();
    }

    public void destroy() {
        viewService.destroy();
    }

}
