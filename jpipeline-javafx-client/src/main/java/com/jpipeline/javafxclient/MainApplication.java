package com.jpipeline.javafxclient;

import com.jpipeline.javafxclient.controller.MainMenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class MainApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

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
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("main_menu.fxml"));
        rootLayout = loader.load();
        Scene scene = new Scene(rootLayout);
        scene.getStylesheets().add(getClass().getResource("main.css").toString());
        primaryStage.setScene(scene);
        MainMenuController mainMenuController = loader.getController();
        mainMenuController.setMain(this);
        primaryStage.show();
        mainMenuController.init();
    }


    public static void main(String[] args) {
        try {
            URL resource = Main.class.getClassLoader().getResource("client.properties");
            Properties properties = new Properties();
            properties.load(new FileInputStream(resource.getFile()));
            JContext.setProperties(properties);
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
        launch(args);
    }
}
