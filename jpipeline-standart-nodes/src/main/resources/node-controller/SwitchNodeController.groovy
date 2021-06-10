import com.jpipeline.common.util.JController
import com.jpipeline.common.util.PropertyConfig
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text

class SwitchNodeController extends JController {

    @FXML
    public TextField propertyField;

    @FXML
    public VBox conditionsContainer;

    private PropertyConfig operatorConfig;
    private PropertyConfig valueConfig;
    private Map<String, String> operatorVariants;

    @Override
    void onInit() {
        def properties = getNodeProperties()

        operatorConfig = nodeConfig.getPropertyConfig("condition").getNested("operator")
        valueConfig = nodeConfig.getPropertyConfig("condition").getNested("value")
        operatorVariants = operatorConfig.getVariants();

        propertyField.setText(properties.getString("property"));

        properties.getList("condition").forEach({ propertyValue ->
            addCondition(propertyValue)
        })
    }

    private void addCondition(Map<String, Object> propertyValue) {
        def operator = propertyValue.get("operator")
        def value = propertyValue.get("value")

        def box = new HBox()

        def operatorChoiceBox = new ChoiceBox<ChoiceObject>();
        operatorChoiceBox.setPrefWidth(50);

        operatorVariants.entrySet().forEach({ entry ->
            def co = new ChoiceObject(entry.getValue(), entry.getKey());
            operatorChoiceBox.getItems().add(co)
            if (co.getValue().equals(operator))
                operatorChoiceBox.getSelectionModel().select(co);
        })

        TextField valueField = new TextField(value.toString())
        valueField.setFocusTraversable(false)

        HBox.setHgrow(valueField, Priority.ALWAYS)

        def toOutputLabel = new Text(createToOutputLabel(conditionsContainer.getChildren().size() + 1))

        def button = new Button("X")
        box.getChildren().add(operatorChoiceBox)
        box.getChildren().add(valueField)
        box.getChildren().add(toOutputLabel)
        box.getChildren().add(button)
        conditionsContainer.getChildren().add(box)

        button.setOnAction({ event ->
            conditionsContainer.getChildren().remove(box);
            updateToOutputLabels();
        })
    }

    private String createToOutputLabel(int idx) {
        return "  → " + idx + "  ";
    }

    private void updateToOutputLabels() {
        int i = 0
        conditionsContainer.getChildren().forEach({ it ->
            if (it instanceof HBox) {
                def toOutputLabel = it.getChildren().get(2);
                if (toOutputLabel instanceof Text)
                    toOutputLabel.setText(createToOutputLabel(++i))
            }
        })
    }

    @Override
    void onClose() {
        def conditions = []
        conditionsContainer.getChildren().forEach({ it ->
            if (it instanceof HBox) {
                def condition = [:]
                def operatorBox = it.getChildren().get(0)
                if (operatorBox instanceof ChoiceBox<ChoiceObject>)
                    condition.put("operator", operatorBox.getValue().getValue())

                def valueField = it.getChildren().get(1)
                if (valueField instanceof TextField)
                    condition.put("value", valueField.getText())

                conditions.add(condition)
            }
        })

        getNodeProperties().put("condition", conditions);
        getNodeProperties().put("property", propertyField.getText());
    }

    @FXML
    void addButtonClick() {
        addCondition(["operator": operatorConfig.getDefaultValue(), "value": valueConfig.getDefaultValue()]);
    }

    private static class ChoiceObject {
        String name;
        Object value;

        String getName() {
            return name
        }

        void setName(String name) {
            this.name = name
        }

        Object getValue() {
            return value
        }

        ChoiceObject(String name, Object value) {
            this.name = name
            this.value = value
        }

        @Override
        String toString() {
            return name;
        }
    }

}
