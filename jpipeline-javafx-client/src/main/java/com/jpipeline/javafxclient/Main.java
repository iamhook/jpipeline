package com.jpipeline.javafxclient;

import com.jpipeline.javafxclient.controller.MainMenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

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
        primaryStage.show();
        mainMenuController.init();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
