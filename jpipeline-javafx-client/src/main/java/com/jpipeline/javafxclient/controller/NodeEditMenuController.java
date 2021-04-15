package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.javafxclient.ui.elements.NodeWrapper;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Setter;

public class NodeEditMenuController {

    @FXML
    public Pane rootPane;

    @Setter
    private Stage stage;

    private NodeWrapper nodeWrapper;

    private NodeDTO node;


    public void init() {



        return;
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
