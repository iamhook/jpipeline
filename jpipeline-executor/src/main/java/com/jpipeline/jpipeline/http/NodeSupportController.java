package com.jpipeline.jpipeline.http;

import com.jpipeline.common.util.NodeTypeConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.jpipeline.service.NodeSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/nodesupport")
public class NodeSupportController {

    @Autowired
    private NodeSupportService nodeSupportService;

    @GetMapping("/types")
    public List<String> getTypes() {
        return nodeSupportService.getNodeTypes();
    }

    @GetMapping("/{type}/config")
    public NodeTypeConfig getNodeConfig(@PathVariable String type) {
        return nodeSupportService.getNodeConfig(type);
    }

    @GetMapping("/{type}/fxml")
    public ResponseEntity getNodeFxml(@PathVariable String type) {
        return getNodeResource(nodeSupportService.getNodeFxml(type));
    }

    @GetMapping("/{type}/html")
    public ResponseEntity getNodeHtml(@PathVariable String type) {
        return getNodeResource(nodeSupportService.getNodeHtml(type));
    }

    @GetMapping("/{type}/controller")
    public ResponseEntity getNodeGroovyController(@PathVariable String type) {
        return getNodeResource(nodeSupportService.getNodeGroovyController(type));
    }

    private ResponseEntity getNodeResource(Resource resource) {
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(resource.getFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header("Content-Disposition", contentDisposition.toString())
                .body(resource);
    }

    @GetMapping("/{type}/create")
    public NodeDTO createNew(@PathVariable String type) {
        return nodeSupportService.createNew(type);
    }


}
