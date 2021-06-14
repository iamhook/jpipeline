package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JController;
import com.jpipeline.common.util.NodeTypeConfig;
import com.jpipeline.common.util.PropertyConfig;
import com.jpipeline.javafxclient.service.NodeService;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StandartNodeEditController extends JController {

    private static final String ID_PREFIX = "property_";

    @Setter
    private Pane rootPane;

    @Override
    public void onInit() {
        CJson nodeProperties = node.getProperties();
        NodeTypeConfig nodeTypeConfig = NodeService.getNodeConfig(node.getType());
        Map<String, PropertyConfig> propertyConfigs = nodeTypeConfig.getProperties().stream().collect(Collectors.toMap(PropertyConfig::getName, pc -> pc));

        try {
            Map<String, Node> propertyNodes = findPropertyNodes(rootPane.getChildren())
                    .stream().collect(Collectors.toMap(n -> n.getId().replace(ID_PREFIX, ""), n -> n));

            for (Map.Entry<String, Node> entry : propertyNodes.entrySet()) {
                String propertyName = entry.getKey();
                Node propertyNode = entry.getValue();
                if (propertyConfigs.containsKey(propertyName)) {
                    PropertyConfig propertyConfig = propertyConfigs.get(propertyName);
                    if (propertyNode instanceof TextInputControl) {
                        TextInputControl textField = (TextInputControl) propertyNode;

                        if (nodeProperties.containsKey(propertyName) && nodeProperties.get(propertyName) != null) {
                            textField.setText(nodeProperties.get(propertyName).toString());
                        }
                        // TextField listener
                        textField.textProperty().addListener((observable, oldValue, newValue) -> {
                            nodeProperties.put(propertyName, newValue);
                        });
                    } else if (propertyNode instanceof ChoiceBox) {
                        ChoiceBox<ChoiceObject> choiceBox = (ChoiceBox<ChoiceObject>) propertyNode;
                        for (Map.Entry<String, String> variant : propertyConfig.getVariants().entrySet()) {
                            ChoiceObject choiceObject = new ChoiceObject(variant.getValue(), variant.getKey());
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

    @Override
    public void onClose() {

    }
}
