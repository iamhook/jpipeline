package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.javafxclient.Main;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.elements.WorkflowService;
import com.jpipeline.javafxclient.ui.util.ViewHelper;
import com.jpipeline.javafxclient.ui.util.Wrapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jpipeline.javafxclient.Consts.*;

public class MainMenuController {

    private static final Logger log = LoggerFactory.getLogger(MainMenuController.class);

    @FXML
    public AnchorPane rootPane;

    @FXML
    public Pane nodesMenu;

    @FXML
    public Pane canvasPane;

    @FXML
    public ScrollPane canvasWrapper;

    @FXML
    public Rectangle managerStatusIndicator;

    @FXML
    public Rectangle executorStatusIndicator;

    public ProgressIndicator progressIndicator;

    private WorkflowService workflowService;

    private Main main;

    private boolean lastExecutorStatus = false;

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void init() {
        executor.scheduleAtFixedRate(this::updateServiceStatuses, 0, 1, TimeUnit.SECONDS);
    }

    @FXML
    public void resetWorkflow() {
        if (workflowService != null) {
            workflowService.destroy();
        }

        WorkflowConfig workflowConfig = NodeService.getConfig();

        workflowService = new WorkflowService(workflowConfig, canvasPane);

        List<String> nodeTypes = NodeService.getNodeTypes();

        int i = 0;
        double offset = 20;
        double margin = 20;
        nodesMenu.getChildren().clear();
        for (String nodeType : nodeTypes) {
            NodeConfig config = NodeService.getNodeConfig(nodeType);
            Rectangle rectangle = ViewHelper.createNodeRectangle(Paint.valueOf(config.getColor()));
            rectangle.setWidth(NODE_WIDTH);
            rectangle.setCursor(Cursor.HAND);
            rectangle.setHeight(NODE_HEIGHT);
            rectangle.setX((nodesMenu.getParent().getLayoutBounds().getWidth() - NODE_WIDTH) / 2);
            rectangle.setY(offset + i * (NODE_HEIGHT + margin));
            Text nameLabel = ViewHelper.createNameLabel(nodeType, rectangle);
            nameLabel.setCursor(Cursor.HAND);
            rectangle.setOnMouseClicked(event -> workflowService.createNode(nodeType));
            nameLabel.setOnMouseClicked(event -> workflowService.createNode(nodeType));
            nodesMenu.getChildren().add(rectangle);
            nodesMenu.getChildren().add(nameLabel);
            i++;
        }
    }

    public void createProgressIndicator() {
        progressIndicator = new ProgressIndicator();
        rootPane.getChildren().add(progressIndicator);

        /*progressIndicator.setVisible(true);*/

        //progressIndicator.setLayoutX(nodesMenu.getWidth() + 30);
        progressIndicator.setManaged(false);
        progressIndicator.resize(PROGRESS_INDICATOR_SIZE, PROGRESS_INDICATOR_SIZE);
        progressIndicator.layoutXProperty().bind(canvasWrapper.widthProperty().divide(2)
                .add(nodesMenu.getWidth())
                .subtract(progressIndicator.getWidth()/2));
        progressIndicator.layoutYProperty().bind(canvasWrapper.heightProperty().divide(2)
                .subtract(progressIndicator.getHeight()/2));
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    private void showProgressIndicator() {
        canvasPane.setDisable(true);
        if (progressIndicator == null) {
            createProgressIndicator();
        }
        GaussianBlur blur = new GaussianBlur(5); // 55 is just to show edge effect more clearly.
        canvasWrapper.setEffect(blur);
        nodesMenu.setEffect(blur);
        progressIndicator.setVisible(true);
    }

    private void hideProgressIndicator() {
        canvasPane.setDisable(false);
        if (progressIndicator == null) {
            createProgressIndicator();
        }
        progressIndicator.setVisible(false);
        canvasWrapper.setEffect(null);
        nodesMenu.setEffect(null);
    }


    private void updateServiceStatuses() {
        if (NodeService.checkIsAlive()) {
            if (!lastExecutorStatus) {
                lastExecutorStatus = true;
                Platform.runLater(this::resetWorkflow);
            }

            Platform.runLater(() -> {
                hideProgressIndicator();
                canvasPane.setDisable(false);
                executorStatusIndicator.setFill(Color.GREEN);
            });
        } else {
            lastExecutorStatus = false;
            Platform.runLater(() -> {
                showProgressIndicator();
                executorStatusIndicator.setFill(Color.RED);
            });
        }

        if (ManagerService.checkIsAlive()) {
            Platform.runLater(() -> managerStatusIndicator.setFill(Color.GREEN));
        } else {
            Platform.runLater(() -> managerStatusIndicator.setFill(Color.RED));
        }
    }

    @FXML
    public void deploy() {
        workflowService.deploy();
    }

    public void setMain(Main main) {
        this.main = main;
    }
}
