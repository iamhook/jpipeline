package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.Main;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.elements.CanvasContext;
import com.jpipeline.javafxclient.ui.elements.WorkflowContextHolder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.List;

public class MainMenuController {

    @FXML
    public AnchorPane rootPane;

    @FXML
    public ListView nodesMenu;

    @FXML
    public Pane canvasPane;

    private WorkflowContextHolder workflowContextHolder;

    private Main main;

    public void init() {
        CanvasContext canvasContext = new CanvasContext(canvasPane);

        WorkflowConfig config = ManagerService.getConfig();

        workflowContextHolder = new WorkflowContextHolder(config, canvasContext);

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
