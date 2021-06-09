import com.jpipeline.common.util.JController
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text

class SwitchNodeController extends JController {

    @FXML
    public VBox messagesContainer;

    @Override
    void onInit() {
        def properties = getNodeProperties()

        properties.getList("message").forEach({property ->
            addMessage(property);
        })
    }

    private void addMessage(String text) {
        def box = new HBox()
        TextField field = new TextField(text)
        field.setFocusTraversable(false)
        field.textProperty().addListener({ observable, oldValue, newValue ->
            updateMessagesProperty()
        } as ChangeListener<? super String>)

        def button = new Button("X")
        box.getChildren().add(field)
        box.getChildren().add(button)
        messagesContainer.getChildren().add(box)

        button.setOnAction({ event ->
            messagesContainer.getChildren().remove(box);
            updateMessagesProperty()
        })
    }

    @Override
    void onClose() {

    }

    private void updateMessagesProperty() {
        def messages = []
        messagesContainer.getChildren().forEach({it ->
            if (it instanceof HBox) {
                def field = it.getChildren().get(0)
                if (field instanceof TextField)
                    messages.add(field.getText())
            }
        })

        getNodeProperties().put("message", messages);
    }

    @FXML
    void addButtonClick() {
        addMessage("");
    }

}