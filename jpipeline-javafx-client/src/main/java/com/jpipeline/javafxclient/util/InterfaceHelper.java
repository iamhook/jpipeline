package com.jpipeline.javafxclient.util;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.JController;
import com.jpipeline.common.util.NodeTypeConfig;
import com.jpipeline.javafxclient.MainApplication;
import com.jpipeline.javafxclient.controller.DebugMenuController;
import com.jpipeline.javafxclient.controller.HtmlNodeEditController;
import com.jpipeline.javafxclient.controller.StandartNodeEditController;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.service.ViewWorkflowService;
import groovy.lang.GroovyClassLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

public class InterfaceHelper {

    private static final Logger log = LoggerFactory.getLogger(InterfaceHelper.class);
    private static final GroovyClassLoader gcl = new GroovyClassLoader();

    public static void showError(String error) {
        log.error(error);
        Alert alert = new Alert(Alert.AlertType.ERROR, error, ButtonType.OK);
        alert.showAndWait();
    }

    public static void showNodeEditMenu(ViewWorkflowService.NodeWrapper nodeWrapper, Window window) {
        try {
            NodeDTO node = nodeWrapper.getNode();
            NodeTypeConfig nodeTypeConfig = NodeService.getNodeConfig(node.getType());

            if (nodeTypeConfig.getEditMode().equals(NodeTypeConfig.EditMode.NONE))
                return;

            JController controller;
            Pane pane;

            if (nodeTypeConfig.getEditMode().equals(NodeTypeConfig.EditMode.HTML_JAVASCRIPT)) {
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("html_node_menu.fxml"));
                pane = loader.load();

                controller = loader.getController();
                ((HtmlNodeEditController) controller).setRootPane(pane);

            } else {
                String fxmlPath = NodeService.getNodeFxml(node.getType());

                if (nodeTypeConfig.getEditMode().equals(NodeTypeConfig.EditMode.FXML_GROOVY)) {
                    String controllerPath = NodeService.getNodeGroovyController(node.getType());
                    Class clazz = gcl.parseClass(new File(controllerPath));
                    controller = (JController) clazz.newInstance();
                } else {
                    controller = new StandartNodeEditController();
                }
                FXMLLoader loader = new FXMLLoader();

                loader.setController(controller);

                if (fxmlPath == null || fxmlPath.isEmpty()) {
                    showError("FXML file not found");
                    return;
                }

                pane = loader.load(new FileInputStream(fxmlPath));

                if (controller instanceof StandartNodeEditController)
                    ((StandartNodeEditController) controller).setRootPane(pane);
            }

            controller.setNode(node);
            controller.setNodeTypeConfig(NodeService.getNodeConfig(node.getType()));

            controller.onInit();
            JController finalController = controller;
            Stage stage = new Stage();

            stage.setOnCloseRequest(event -> {
                finalController.onClose();
            });

            stage.setScene(new Scene(pane));
            stage.setTitle(node.getType() + " edit menu");
            stage.initOwner(window);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();

            stage.setMinHeight(stage.getHeight());
            stage.setMinWidth(stage.getWidth());
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Stage debugStage;
    private static DebugMenuController debugMenuController;

    public static void createDebugMenu(Window window) {
        try {
            debugStage = new Stage();
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("debug_menu.fxml"));
            Parent root = loader.load();
            debugStage.setScene(new Scene(root));
            debugStage.initModality(Modality.NONE);
            debugStage.initOwner(window);
            debugStage.setHeight(700);
            debugStage.setWidth(700);
            debugStage.setTitle("Debug");

            debugMenuController = loader.getController();
            debugMenuController.setStage(debugStage);
            debugMenuController.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDebugMenu() {
        debugStage.show();
    }

    public static void resetDebugSignalSubscription() {
        debugMenuController.resetSignalSubscription();
    }

}
