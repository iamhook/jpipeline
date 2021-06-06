package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.common.util.PropertyConfig;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.NodeWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeEditMenuController {

    private static final String ID_PREFIX = "property_";

    @FXML
    private Pane wrapper;

    @Setter
    private Stage stage;

    private NodeWrapper nodeWrapper;

    private NodeDTO node;


    public void init() {
        CJson nodeProperties = node.getProperties();
        NodeConfig nodeConfig = NodeService.getNodeConfig(node.getType());
        Map<String, PropertyConfig> propertyConfigs = nodeConfig.getProperties().stream().collect(Collectors.toMap(pc -> pc.getName(), pc -> pc));

        try {
            String fxmlPath = NodeService.getNodeFxml(node.getType());

            FXMLLoader loader = new FXMLLoader();
            Pane pane = loader.load(new FileInputStream(fxmlPath));
            stage.setWidth(pane.getPrefWidth());
            stage.setHeight(600);
            stage.centerOnScreen();
            wrapper.getChildren().add(pane);


            Map<String, Node> propertyNodes = findPropertyNodes(pane.getChildren())
                    .stream().collect(Collectors.toMap(n -> n.getId().replace(ID_PREFIX, ""), n -> n));

            for (Map.Entry<String, Node> entry : propertyNodes.entrySet()) {
                String propertyName = entry.getKey();
                Node propertyNode = entry.getValue();
                if (propertyConfigs.containsKey(propertyName)) {
                    PropertyConfig propertyConfig = propertyConfigs.get(propertyName);
                    if (propertyNode instanceof TextField) {
                        TextField textField = (TextField) propertyNode;

                        if (nodeProperties.containsKey(propertyName)) {
                            textField.setText(nodeProperties.get(propertyName).toString());
                        }
                        // TextField listener
                        textField.textProperty().addListener((observable, oldValue, newValue) -> {
                            nodeProperties.put(propertyName, newValue);
                        });
                    } else if (propertyNode instanceof ChoiceBox) {
                        ChoiceBox<ChoiceObject> choiceBox = (ChoiceBox<ChoiceObject>) propertyNode;
                        for (Object variant : propertyConfig.getVariants()) {
                            ChoiceObject choiceObject = new ChoiceObject(variant.toString(), variant.toString());
                            choiceBox.getItems().add(choiceObject);
                            if (nodeProperties.containsKey(propertyName)) {
                                if (nodeProperties.get(propertyName).equals(choiceObject.getValue())) {
                                    choiceBox.getSelectionModel().select(choiceObject);
                                }
                            }
                        }

                        // ChoiceBox listener
                        choiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                            nodeProperties.put(propertyName, newValue.getValue());
                        });
                    }

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Node> findPropertyNodes(List<Node> children) {
        List<Node> properties = new ArrayList<>();
        for (Node node : children) {
            if (node.getId() != null && node.getId().startsWith(ID_PREFIX)) {
                properties.add(node);
            } else {
                if (node instanceof Pane) {
                    properties.addAll(findPropertyNodes(((Pane) node).getChildren()));
                }
            }
        }
        return properties;
    }

    @Getter @Setter @AllArgsConstructor
    private static class ChoiceObject {
        String name;
        Object value;

        @Override
        public String toString() {
            return name;
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
