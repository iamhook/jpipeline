package com.jpipeline.javafxclient.controller;

import com.jpipeline.javafxclient.service.AuthContext;
import com.jpipeline.javafxclient.service.ManagerService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionMenuController {

    private static final Logger log = LoggerFactory.getLogger(ConnectionMenuController.class);

    @Setter
    private MainMenuController mainMenuController;

    @FXML
    public Pane hostMenu;

    @FXML
    public Pane loginMenu;

    @FXML
    public TextField hostNameField;

    @FXML
    public Text errorText;

    public void connect() {

        String hostName = hostNameField.getText();

        if (hostName != null && !hostName.isEmpty()) {
            AuthContext.setManagerHost(hostName);
            ManagerService.createHttpService();

            boolean managerIsAlive = ManagerService.checkIsAlive();

            if (!managerIsAlive) {
                errorText.setText("Manager is not reachable");
            }
            else {
                errorText.setText("");
                showLoginMenu();
            }

        } else {
            errorText.setText("Hostname shouldn't be empty");
        }

    }

    public void login() {
        mainMenuController.connectionSuccessCallback();
    }


    public void showLoginMenu() {
        hostMenu.setVisible(false);
        loginMenu.setVisible(true);
    }

}
