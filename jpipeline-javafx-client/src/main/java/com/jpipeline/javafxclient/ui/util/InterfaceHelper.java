package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.MainApplication;
import com.jpipeline.javafxclient.controller.DebugMenuController;
import com.jpipeline.javafxclient.controller.NodeEditMenuController;
import com.jpipeline.javafxclient.ui.elements.NodeWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class InterfaceHelper {

    public static void showNodeEditMenu(NodeWrapper nodeWrapper, Window window) {
        try {
            NodeDTO node = nodeWrapper.getNode();
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("node_edit_menu.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(node.getType() + " edit menu");
            stage.initOwner(window);
            stage.setHeight(700);
            stage.setWidth(700);
            stage.show();

            NodeEditMenuController controller = loader.getController();
            controller.setStage(stage);
            controller.setNodeWrapper(nodeWrapper);
            controller.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Stage debugStage;
    private static DebugMenuController debugMenuController;

    public static void createDebugMenu(Window window) {
        try {
            debugStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApplication.class.getResource("debug_menu.fxml"));
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
