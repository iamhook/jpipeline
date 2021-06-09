package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JController;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.common.util.PropertyConfig;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.NodeWrapper;
import groovy.lang.GroovyClassLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
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
            String controllerPath = NodeService.getNodeFxmlController(node.getType());
            JController controller = null;

            GroovyClassLoader gcl = new GroovyClassLoader();

            FXMLLoader loader = new FXMLLoader();

            if (controllerPath != null && !controllerPath.isEmpty()) {
                Class clazz = gcl.parseClass(new File(controllerPath));
                controller = (JController) clazz.newInstance();
                controller.setNodeDTO(node);
                loader.setController(controller);
            }

            Pane pane = loader.load(new FileInputStream(fxmlPath));

            if (controller != null)
                controller.onInit();

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

    @FXML
    public void closeModal() {
        stage.close();
    }

    public void setNodeWrapper(NodeWrapper nodeWrapper) {
        this.node = nodeWrapper.getNode();
        this.nodeWrapper = nodeWrapper;
    }
}
