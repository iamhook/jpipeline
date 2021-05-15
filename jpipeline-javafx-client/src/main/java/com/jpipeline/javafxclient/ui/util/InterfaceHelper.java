package com.jpipeline.javafxclient.ui.util;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.Main;
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
            loader.setLocation(Main.class.getResource("node_edit_menu.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(node.getType() + " edit menu");
            stage.initModality(Modality.WINDOW_MODAL);
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
}
