package com.jpipeline.javafxclient;

import com.jpipeline.javafxclient.controller.MainMenuController;
import com.jpro.webapi.JProApplication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends JProApplication {

    private Stage primaryStage;
    private AnchorPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            this.primaryStage.setTitle("JPipeline");
            initRootLayout();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void initRootLayout() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("main_menu.fxml"));
        rootLayout = loader.load();
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        MainMenuController mainMenuController = loader.getController();
        mainMenuController.setMain(this);
        mainMenuController.init();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
