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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainMenuController {

    @FXML
    public AnchorPane rootPane;

    @FXML
    public ListView nodesMenu;

    @FXML
    public Pane canvasPane;

    @FXML
    public Rectangle managerStatusIndicator;

    @FXML
    public Rectangle executorStatusIndicator;

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

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(this::updateServiceStatuses, 1, 5, TimeUnit.SECONDS);

    }

    private void updateServiceStatuses() {
        if (NodeService.checkIsAlive()) {
            executorStatusIndicator.setFill(Color.GREEN);
        } else {
            executorStatusIndicator.setFill(Color.RED);
        }
        if (ManagerService.checkIsAlive()) {
            managerStatusIndicator.setFill(Color.GREEN);
        } else {
            managerStatusIndicator.setFill(Color.RED);
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
