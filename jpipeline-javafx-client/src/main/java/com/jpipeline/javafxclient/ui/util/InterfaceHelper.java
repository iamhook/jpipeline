package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.JController;
import com.jpipeline.javafxclient.MainApplication;
import com.jpipeline.javafxclient.controller.DebugMenuController;
import com.jpipeline.javafxclient.controller.StandartNodeEditController;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.ViewWorkflowService;
import groovy.lang.GroovyClassLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;

public class InterfaceHelper {

    private static final GroovyClassLoader gcl = new GroovyClassLoader();

    public static void showNodeEditMenu(ViewWorkflowService.NodeWrapper nodeWrapper, Window window) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader();


            NodeDTO node = nodeWrapper.getNode();
            String fxmlPath = NodeService.getNodeFxml(node.getType());
            String controllerPath = NodeService.getNodeFxmlController(node.getType());
            JController controller = null;

            if (controllerPath != null && !controllerPath.isEmpty()) {
                Class clazz = gcl.parseClass(new File(controllerPath));
                controller = (JController) clazz.newInstance();
            } else {
                controller = new StandartNodeEditController();
            }

            controller.setAddOutputCallback(() -> {
                nodeWrapper.getNode().getOutputs().add(new HashSet<>());
                nodeWrapper.addOutput();
                nodeWrapper.fixOutputHandlesPositions();
            });
            controller.setNode(node);
            controller.setNodeConfig(NodeService.getNodeConfig(node.getType()));

            loader.setController(controller);

            Pane pane = loader.load(new FileInputStream(fxmlPath));

            if (controller instanceof StandartNodeEditController)
                ((StandartNodeEditController) controller).setRootPane(pane);

            if (controller != null) {
                controller.onInit();
                JController finalController = controller;
                stage.setOnCloseRequest(event -> {
                    finalController.onClose();
                });
            }

            stage.setScene(new Scene(pane));
            stage.setTitle(node.getType() + " edit menu");
            stage.initOwner(window);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();

            stage.setWidth(pane.getPrefWidth());
            stage.setHeight(600);
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
