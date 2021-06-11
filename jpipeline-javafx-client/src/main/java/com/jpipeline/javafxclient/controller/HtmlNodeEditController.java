package com.jpipeline.javafxclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JController;
import com.jpipeline.javafxclient.service.NodeService;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import netscape.javascript.JSObject;

import java.io.File;
import java.util.function.Consumer;

public class HtmlNodeEditController extends JController {

    private static final ObjectMapper OM = new ObjectMapper();

    static {
        com.sun.javafx.webkit.WebConsoleListener.setDefaultListener((view, message, lineNumber, sourceId) -> {
            System.out.println(message + "[at " + lineNumber + "]");
        });
    }

    @Setter
    private Pane rootPane;

    @FXML
    public WebView webView;

    private JSObject bridge;

    private OutputProcessor outputProcessor;

    @Override
    public void onInit() {
        outputProcessor = new OutputProcessor(addOutputCallback, removeOutputCallback);
        CJson nodeJson = OM.convertValue(node, CJson.class);
        CJson nodeConfigJson = OM.convertValue(nodeConfig, CJson.class);

        String htmlPath = NodeService.getNodeHtml(node.getType());

        bridge = (JSObject) webView.getEngine()
                .executeScript("window");

        bridge.setMember("outputProcessor", outputProcessor);
        bridge.setMember("node", nodeJson.toJson());
        bridge.setMember("nodeConfig", nodeConfigJson.toJson());
        webView.getEngine().executeScript("node = JSON.parse(node)");
        webView.getEngine().executeScript("nodeConfig = JSON.parse(nodeConfig)");
        //webView.getEngine().executeScript("addNodeOutput = addNodeOutput.run");

        webView.getEngine().load(new File(htmlPath).toURI().toString());

    }

    @AllArgsConstructor
    public class OutputProcessor {
        protected Runnable addOutputCallback;
        protected Consumer<Integer> removeOutputCallback;

        public void addOutput() {
            addOutputCallback.run();
        }

        public void removeOutput(Integer idx) {
            removeOutputCallback.accept(idx);
        }

    }

    @Override
    public void onClose() {
        CJson nodeJson = CJson.fromJson((String)webView.getEngine().executeScript("JSON.stringify(node)"));

        node.setProperties(nodeJson.getJson("properties"));

        return;
    }
}
