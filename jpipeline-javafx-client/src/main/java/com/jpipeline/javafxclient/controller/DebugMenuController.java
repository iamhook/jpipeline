package com.jpipeline.javafxclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.CJson;
import com.jpipeline.javafxclient.service.NodeService;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.util.Map;

public class DebugMenuController {

    private static final Logger log = LoggerFactory.getLogger(DebugMenuController.class);

    @FXML
    private ListView nodeDebugListView;

    @Setter
    private Stage stage;

    private Disposable signalSubscribe;

    public void init() {

    }

    public void resetSignalSubscription() {
        if (signalSubscribe != null)
            signalSubscribe.dispose();

        try {
            signalSubscribe = NodeService.getSignalStream()
                    .filter(nodeSignal -> nodeSignal.getType().equals(Node.SignalType.DEBUG)
                            || nodeSignal.getType().equals(Node.SignalType.ERROR))
                    .filter(nodeSignal -> nodeSignal.getBody() != null)
                    .subscribe(nodeSignal -> {
                        String message;
                        if (nodeSignal.getBody() instanceof CJson)
                            message = ((CJson) nodeSignal.getBody()).toJson();
                        else if (nodeSignal.getBody() instanceof Map)
                            message = new CJson((Map) nodeSignal.getBody()).toJson();
                        else
                            message = nodeSignal.getBody().toString();

                        Text msg = new Text(message);
                        if (nodeSignal.getType().equals(Node.SignalType.ERROR)) {
                            msg.setFill(Color.RED);
                        }
                        Platform.runLater(() -> {
                            nodeDebugListView.getItems().add(msg);
                            nodeDebugListView.scrollTo(msg);
                        });
                    });
        } catch (JsonProcessingException e) {
            log.error(e.toString(), e);
        }
    }

    @FXML
    public void clearNodeDebug() {
        nodeDebugListView.getItems().clear();
    }

    @FXML
    public void clearExecutorLogs() {

    }

    @FXML
    public void closeModal() {
        stage.close();
    }
}
