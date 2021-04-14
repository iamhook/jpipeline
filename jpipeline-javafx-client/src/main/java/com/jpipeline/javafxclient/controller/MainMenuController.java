package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.javafxclient.Main;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.elements.WorkflowService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.List;

public class MainMenuController {

    @FXML
    public AnchorPane rootPane;

    @FXML
    public ListView nodesMenu;

    @FXML
    public Pane canvasPane;

    private WorkflowService workflowContextHolder;

    private Main main;

    public void init() {
        WorkflowConfig config = ManagerService.getConfig();

        workflowContextHolder = new WorkflowService(config, canvasPane);

        List<String> nodeTypes = NodeService.getNodeTypes();

        for (String nodeType : nodeTypes) {
            Button nodeButton = new Button(nodeType);
            nodeButton.setOnAction(event -> workflowContextHolder.createNode(nodeType));
            nodesMenu.getItems().add(nodeButton);
        }



    }

    @FXML
    public void deploy() {
        workflowContextHolder.deploy();
    }

    public void setMain(Main main) {
        this.main = main;
    }
}
