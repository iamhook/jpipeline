package com.jpipeline.jpipeline.http;

import com.jpipeline.jpipeline.service.WorkflowContext;
import com.jpipeline.jpipeline.util.CJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowContext workflowContext;

    @PostMapping("/deploy")
    public void deploy(@RequestBody CJson config) {
        workflowContext.deploy(config);
    }

    @GetMapping("/deploy")
    public void deploy() throws Exception {
        workflowContext.deploy();
    }

}
