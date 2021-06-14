package com.jpipeline.javafxclient.controller;

import com.jpipeline.javafxclient.context.PropertiesStore;
import com.jpipeline.javafxclient.context.AuthContext;
import com.jpipeline.javafxclient.service.JConnection;
import com.jpipeline.javafxclient.service.ManagerService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ConnectionMenuController {

    private static final Logger log = LoggerFactory.getLogger(ConnectionMenuController.class);

    @Setter
    private MainMenuController mainMenuController;

    @FXML
    public Pane loginMenu;

    @FXML
    public TextField hostnameField;

    @FXML
    public TextField portField;

    @FXML
    public TextField usernameField;

    @FXML
    public TextField passwordField;

    @FXML
    public ListView<JConnection> connectionsList;

    @FXML
    public CheckBox saveConnectionBox;

    @FXML
    public Text errorText;

    public void init() {
        Set<JConnection> connections = PropertiesStore.getConnections();
        for (JConnection connection : connections) {
            connectionsList.getItems().add(connection);
        }
    }

    public void openConnection() {
        JConnection connection = connectionsList.getSelectionModel().getSelectedItem();
        if (connection == null) return;
        hostnameField.setText(connection.getHostname());
        portField.setText(connection.getPort() == null ? "" : connection.getPort().toString());
        usernameField.setText(connection.getUsername());
        passwordField.setText(connection.getPassword());
    }

    public void deleteConnection() {
        JConnection connection = connectionsList.getSelectionModel().getSelectedItem();
        PropertiesStore.deleteConnection(connection);
        connectionsList.getItems().remove(connection);
    }

    public void login() {
        errorText.setText("");

        JConnection connection = new JConnection();

        String hostname = hostnameField.getText();
        String port = portField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        String host = hostname + (port == null? "" : ":" + port);

        if (hostname != null && !hostname.isEmpty()) {

            boolean managerIsAlive = ManagerService.checkIsAlive(host);

            if (!managerIsAlive) {
                errorText.setText("Manager " + host + " is not reachable");
                return;
            }

        } else {
            errorText.setText("Hostname shouldn't be empty");
            return;
        }

        connection.setHostname(hostname);

        if (port != null && !port.isEmpty())
            connection.setPort(Integer.parseInt(port));

        if (username != null && !username.isEmpty()) {
            connection.setUsername(username);
        }

        if (password != null && !password.isEmpty()) {
            connection.setPassword(password);
        }

        AuthContext.setConnection(connection);
        ManagerService.createHttpService();

        try {
            if (ManagerService.login()) {
                mainMenuController.connectionSuccessCallback();
                if (saveConnectionBox.isSelected())
                    PropertiesStore.saveConnection(connection);
            }
        } catch (Exception e) {
            errorText.setText(e.getMessage());
        }

    }

}
