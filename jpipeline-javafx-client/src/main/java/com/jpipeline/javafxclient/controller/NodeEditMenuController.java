package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.ui.elements.NodeWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.Map;

public class NodeEditMenuController {

    @FXML
    private Pane rootPane;

    @FXML
    private GridPane gridPane;

    @Setter
    private Stage stage;

    private NodeWrapper nodeWrapper;

    private NodeDTO node;


    public void init() {

        int i = 0;
        TextField nameField = new TextField(node.getType());
        gridPane.addRow(i++, new Text("Name"), nameField);

        for (Map.Entry<String, Object> entry : node.getProperties().entrySet()) {
            Text propertyName = new Text(entry.getKey());

            gridPane.add(propertyName, 0, i);

            String value = "";

            if (entry.getValue() != null) {
                value = entry.getValue().toString();
            }
            TextField propertyValue = new TextField(value);
            gridPane.add(propertyValue, 1, i);

            i++;
        }
    }


    @FXML
    public void closeModal() {
        stage.close();
    }

    public void setNodeWrapper(NodeWrapper nodeWrapper) {
        this.node = nodeWrapper.getNode();
        this.nodeWrapper = nodeWrapper;
    }
}
