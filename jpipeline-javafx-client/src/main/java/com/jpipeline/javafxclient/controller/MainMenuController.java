package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.javafxclient.MainApplication;
import com.jpipeline.javafxclient.service.ManagerService;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.service.ViewWorkflowService;
import com.jpipeline.javafxclient.util.CanvasHelper;
import com.jpipeline.javafxclient.util.InterfaceHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jpipeline.javafxclient.Consts.*;

public class MainMenuController {

    private static final Logger log = LoggerFactory.getLogger(MainMenuController.class);

    @FXML
    public AnchorPane rootPane;

    @FXML
    public VBox nodesMenu;

    @FXML
    public Pane canvasPane;

    @FXML
    public Pane statusPane;

    @FXML
    public ScrollPane canvasWrapper;

    @FXML
    public Rectangle managerStatusIndicator;

    @FXML
    public Rectangle executorStatusIndicator;

    @FXML
    public Button resetButton;

    @FXML
    public Button deployButton;

    private ProgressIndicator progressIndicator;

    private ViewWorkflowService workflowService;
    private WorkflowConfig workflowConfig;

    private Stage loginStage;

    private MainApplication main;

    private boolean lastExecutorStatus = false;

    private ScheduledFuture<?> statusesTaskFuture;

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void init() {
        showConnectionMenu();
        InterfaceHelper.createDebugMenu(rootPane.getScene().getWindow());
    }

    private void startCheckStatusesTask() {
        statusesTaskFuture = executor.scheduleAtFixedRate(this::updateServiceStatuses, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void stopCheckStatusesTask() {
        statusesTaskFuture.cancel(true);
    }

    public void showConnectionMenu() {
        try {
            blurWorkArea();
            blurStatusPane();
            loginStage = new Stage();
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("connection_menu.fxml"));
            Parent root = loader.load();

            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Connection menu");
            loginStage.initOwner(rootPane.getScene().getWindow());
            loginStage.setOnCloseRequest(windowEvent -> {
                if (!ManagerService.checkIsAlive())
                    windowEvent.consume();
            });
            loginStage.setResizable(false);
            loginStage.show();
            ConnectionMenuController controller = loader.getController();
            controller.setMainMenuController(this);
            controller.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hideConnectionMenu() {
        loginStage.close();
        loginStage = null;
        startCheckStatusesTask();
        unBlur();
    }

    public void connectionSuccessCallback() {
        hideConnectionMenu();
        showProgressIndicator();
        lastExecutorStatus = false;

    }

    private static final List<String> categoriesOrder = Arrays.asList(
            "Other", "Util", "Sequence", "Function", "Common"
    );


    @FXML
    public void resetWorkflow() {

        if (workflowService != null) {
            workflowService.destroy();
        }

        workflowConfig = NodeService.getConfig();


        workflowService = new ViewWorkflowService(canvasPane, workflowConfig);

        double offset = 10;
        double margin = 10;
        nodesMenu.getChildren().clear();

        List<Map.Entry<String, List<NodeConfig>>> categories =
                NodeService.getNodeTypes().stream().sorted().map(NodeService::getNodeConfig)
                .collect(Collectors.groupingBy(NodeConfig::getCategory)).entrySet().stream()
                        .sorted((o1, o2) -> {
                            int i1 = categoriesOrder.indexOf(o1.getKey());
                            int i2 = categoriesOrder.indexOf(o2.getKey());
                            return Integer.compare(i2, i1);
                        }).collect(Collectors.toList());

        for (Map.Entry<String, List<NodeConfig>> entry : categories) {
            AnchorPane categoryPane = new AnchorPane();
            String categoryName = entry.getKey();
            int i = 0;
            List<NodeConfig> nodeConfigs = entry.getValue();

            for (NodeConfig config : nodeConfigs) {
                Rectangle rectangle = CanvasHelper.createNodeRectangle(Paint.valueOf(config.getColor()));
                rectangle.setWidth(NODE_BASE_WIDTH);
                rectangle.setCursor(Cursor.HAND);
                rectangle.setHeight(NODE_BASE_HEIGHT);
                rectangle.setX((nodesMenu.getParent().getLayoutBounds().getWidth() - NODE_BASE_WIDTH) / 2);
                rectangle.setY(offset + i * (NODE_BASE_HEIGHT + margin));
                Text nameLabel = CanvasHelper.createNameLabel(config.getName(), rectangle);
                nameLabel.setCursor(Cursor.HAND);
                rectangle.setOnMouseClicked(event -> createNode(config.getName()));
                nameLabel.setOnMouseClicked(event -> createNode(config.getName()));
                categoryPane.getChildren().add(rectangle);
                categoryPane.getChildren().add(nameLabel);
                i++;
            }

            nodesMenu.getChildren().add(new TitledPane(categoryName, categoryPane));
        }


    }

    private void createNode(String nodeType) {
        NodeDTO node = NodeService.createNewNode(nodeType);
        workflowService.createNode(node, false);
    }

    public void createProgressIndicator() {
        progressIndicator = new ProgressIndicator();
        rootPane.getChildren().add(progressIndicator);
        progressIndicator.setManaged(false);
        progressIndicator.resize(PROGRESS_INDICATOR_SIZE, PROGRESS_INDICATOR_SIZE);
        progressIndicator.layoutXProperty().bind(rootPane.widthProperty().divide(2)
                .subtract(progressIndicator.getWidth()/2));
        progressIndicator.layoutYProperty().bind(rootPane.heightProperty().divide(2)
                .subtract(progressIndicator.getHeight()/2));
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setStyle("-fx-accent: gray;");
    }

    private void blurWorkArea() {
        GaussianBlur blur = new GaussianBlur(5);
        canvasWrapper.setEffect(blur);
        nodesMenu.setEffect(blur);
    }

    private void blurStatusPane() {
        GaussianBlur blur = new GaussianBlur(5);
        statusPane.setEffect(blur);
    }

    private void unBlur() {
        canvasWrapper.setEffect(null);
        nodesMenu.setEffect(null);
        statusPane.setEffect(null);
    }

    private void showProgressIndicator() {
        canvasPane.setDisable(true);
        resetButton.setDisable(true);
        deployButton.setDisable(true);
        if (progressIndicator == null) {
            createProgressIndicator();
        }
        blurWorkArea();
        progressIndicator.setVisible(true);
    }

    private void hideProgressIndicator() {
        canvasPane.setDisable(false);
        resetButton.setDisable(false);
        deployButton.setDisable(false);
        if (progressIndicator == null) {
            createProgressIndicator();
        }
        progressIndicator.setVisible(false);
        unBlur();
    }


    private void updateServiceStatuses() {
        try {
            if (NodeService.checkIsAlive()) {
                if (!lastExecutorStatus) {
                    lastExecutorStatus = true;
                    NodeService.flushSignalFlux();
                    InterfaceHelper.resetDebugSignalSubscription();
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

            Platform.runLater(() -> {
                if (ManagerService.checkIsAlive()) {
                    managerStatusIndicator.setFill(Color.GREEN);
                } else {
                    managerStatusIndicator.setFill(Color.RED);
                    if (loginStage == null || !loginStage.isShowing()) {
                        stopCheckStatusesTask();
                        showConnectionMenu();
                    }
                }
            });
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    @FXML
    public void openDebugMenu() {
        InterfaceHelper.showDebugMenu();
    }

    @FXML
    public void deploy() {
        ManagerService.deploy(workflowConfig);
        lastExecutorStatus = false;
        showProgressIndicator();
        executorStatusIndicator.setFill(Color.RED);
    }

    public void setMain(MainApplication main) {
        this.main = main;
    }
}
