package com.jpipeline.javafxclient;

import com.jpipeline.common.util.CJson;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        try {
            CJson json = new CJson();
            this.primaryStage = primaryStage;
            this.primaryStage.setTitle("Search in files");
            /*final CJson cJson = new CJson();
            cJson.put("sadad","asdasd");*/
            // инициализируем root
            initRootLayout();
            // показываем основную часть приложения
            showMainMenu();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void initRootLayout() throws IOException {
        rootLayout = FXMLLoader.load(getClass().getResource("root.fxml"));
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.setHeight(500);
        primaryStage.setWidth(750);
        primaryStage.show();
    }
    private void showMainMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("main_menu.fxml"));
        AnchorPane mainMenu = loader.load();
        rootLayout.setCenter(mainMenu);
        MainMenuController mainMenuController = loader.getController();
        mainMenuController.setMain(this);
        mainMenuController.init();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
