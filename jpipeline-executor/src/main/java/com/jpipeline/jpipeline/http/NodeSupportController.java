package com.jpipeline.jpipeline.http;

import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.jpipeline.service.NodeSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public NodeConfig getProperties(@PathVariable String type) {
        return nodeSupportService.getNodeConfig(type);
    }

    @GetMapping("/{type}/create")
    public NodeDTO createNew(@PathVariable String type) {
        return nodeSupportService.createNew(type);
    }


}
