package com.jpipeline.javafxclient.controller;

import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.common.util.PropertyConfig;
import com.jpipeline.javafxclient.service.NodeService;
import com.jpipeline.javafxclient.ui.NodeWrapper;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
        NodeConfig nodeConfig = NodeService.getNodeConfig(node.getType());

        showProperties(gridPane, nodeConfig.getProperties(), node.getProperties());
    }

    // TODO rename
    private void showProperties(GridPane gridPane, List<PropertyConfig> propertyConfigs, CJson nodeProperties) {
        int i = 0;
        TextField nameField = new TextField(node.getType());
        gridPane.addRow(i++, new Text("Name"), nameField);

        for (PropertyConfig property : propertyConfigs) {
            String propertyName = property.getName();


            Text propertyNameField = new Text(propertyName);
            gridPane.add(propertyNameField, 0, i);

            Collection valueCollection;
            if (property.isMultiple()) {
                valueCollection = (Collection) nodeProperties.get(propertyName);
            }
            else {
                valueCollection = Collections.singletonList(nodeProperties.get(propertyName));
            }

            AtomicInteger j = new AtomicInteger();
            GridPane propertyGridPane = new GridPane();
            gridPane.add(propertyGridPane, 1, i++);

            Runnable reloadValue = () -> {
                valueCollection.clear();
                propertyGridPane.getChildren().stream()
                        .filter(n -> n instanceof TextField)
                        .forEach(n -> {
                            String text = ((TextField) n).getText();
                            if (text != null && !text.isEmpty()) {
                                valueCollection.add(text);
                            }
                        });
            };

            ChangeListener propertyListener;
            if (property.isMultiple()) {
                propertyListener = (observable, oldValue, newValue) -> {
                    /*if (property.isNumber()) {
                        if (!newValue.matches("\\d*")) {
                            newValue = newValue.replaceAll("[^\\d]", "");
                        }
                    }*/
                    //propertyValueField.setText(newValue);
                    reloadValue.run();
                };
            } else {
                propertyListener = (observable, oldValue, newValue) -> {
                    if (property.isEnumeration()) {
                        ChoiceObject choiceObject = (ChoiceObject) newValue;
                        nodeProperties.put(propertyName, choiceObject.getValue());
                    } else {
                        nodeProperties.put(propertyName, newValue);
                    }
                    /*if (property.isNumber()) {
                        if (!newValue.matches("\\d*")) {
                            newValue = newValue.replaceAll("[^\\d]", "");
                        }
                    }*/
                    //propertyValueField.setText(newValue);
                };
            }

            Consumer<Object> createField = (val) -> {
                if (property.isMultiple()) {
                    Button deleteRowButton = new Button("-");
                    int j1 = j.get();
                    deleteRowButton.setOnAction(event -> {
                        propertyGridPane.getChildren().removeIf(n -> GridPane.getRowIndex(n) == j1);
                        reloadValue.run();
                    });
                    propertyGridPane.add(deleteRowButton, 0, j.get());
                }

                if (property.isEnumeration()) {
                    ChoiceBox<ChoiceObject> propertyChoiceBox = new ChoiceBox<>();

                    for (Object variant : property.getVariants()) {
                        ChoiceObject choiceObject = new ChoiceObject(variant.toString(), variant);
                        propertyChoiceBox.getItems().add(choiceObject);
                        if (val.equals(variant))
                            propertyChoiceBox.getSelectionModel().select(choiceObject);
                    }
                    propertyGridPane.add(propertyChoiceBox, 1, j.getAndIncrement());
                    propertyChoiceBox.valueProperty().addListener(propertyListener);
                } else {
                    TextField propertyValueField = new TextField(val.toString());

                    propertyGridPane.add(propertyValueField, 1, j.getAndIncrement());
                    propertyValueField.textProperty().addListener(propertyListener);
                }

            };

            if (property.isMultiple()) {
                Button addRowButton = new Button("+");
                addRowButton.setOnAction(event -> createField.accept(""));
                gridPane.add(addRowButton, 1, i);
            }

            for (Object value : valueCollection) {
                if (property.isComplex()) {
                    GridPane subGridPane = new GridPane();
                    propertyGridPane.add(subGridPane, 1, j.getAndIncrement());
                    Button deleteRowButton = new Button("-");
                    int j1 = j.get();
                    deleteRowButton.setOnAction(event -> {
                        propertyGridPane.getChildren().removeIf(n -> GridPane.getRowIndex(n) == j1);
                        reloadValue.run();
                    });
                    propertyGridPane.add(deleteRowButton, 0, j.get());
                    showProperties(subGridPane, property.getNested(), new CJson((Map)value));

                } else {
                    String propertyValue = "";
                    if (value != null) {
                        propertyValue = value.toString();
                    }
                    createField.accept(propertyValue);
                }
            }

            /*if (property.isMultiple()) {

            } else {
                Object value = nodeProperties.get(propertyName);

                String propertyValue = "";
                if (value != null) {
                    propertyValue = value.toString();
                }

                TextField propertyValueField = new TextField(propertyValue);

                gridPane.add(propertyValueField, 1, i);

                propertyValueField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (property.isNumber()) {
                        if (!newValue.matches("\\d*")) {
                            newValue = newValue.replaceAll("[^\\d]", "");
                        }
                    }

                    propertyValueField.setText(newValue);
                    nodeProperties.put(propertyName, newValue);
                });
            }*/

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

    @Getter @Setter @AllArgsConstructor
    private static class ChoiceObject {
        String name;
        Object value;

        @Override
        public String toString() {
            return name;
        }
    }
}
