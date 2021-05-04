package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.javafxclient.Main;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.elements.WorkflowService;
import com.jpipeline.javafxclient.ui.util.ViewHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jpipeline.javafxclient.Consts.NODE_HEIGHT;
import static com.jpipeline.javafxclient.Consts.NODE_WIDTH;

public class MainMenuController {

    private static final Logger log = LoggerFactory.getLogger(MainMenuController.class);

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

    private boolean lastExecutorStatus = false;

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void init() {
        executor.scheduleAtFixedRate(this::updateServiceStatuses, 1, 1, TimeUnit.SECONDS);
    }

    // TODO rename it!
    public void refresh() {
        WorkflowConfig workflowConfig = ManagerService.getConfig();

        workflowContextHolder = new WorkflowService(workflowConfig, canvasPane);

        List<String> nodeTypes = NodeService.getNodeTypes();

        for (String nodeType : nodeTypes) {
            NodeConfig config = NodeService.getNodeConfig(nodeType);
            Rectangle rectangle = ViewHelper.createNodeRectangle(Paint.valueOf(config.getColor()));
            rectangle.setWidth(NODE_WIDTH);
            rectangle.setHeight(NODE_HEIGHT);
            Text nameLabel = ViewHelper.createNameLabel(nodeType, rectangle);
            rectangle.setOnMouseClicked(event -> workflowContextHolder.createNode(nodeType));
            nodesMenu.getItems().add(rectangle);
        }
    }

    private void updateServiceStatuses() {
        if (NodeService.checkIsAlive()) {
            if (!lastExecutorStatus) {
                lastExecutorStatus = true;
                Platform.runLater(() -> {
                    if (workflowContextHolder != null) {
                        workflowContextHolder.destroy();
                    }
                    refresh();
                });
            }
            executorStatusIndicator.setFill(Color.GREEN);
        } else {
            lastExecutorStatus = false;
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
