package com.jpipeline.jpipeline.http;

import com.jpipeline.jpipeline.service.WorkflowContext;
import com.jpipeline.jpipeline.util.CJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowContext workflowContext;

    @PostMapping
    public void deploy(@RequestBody CJson config) {
        workflowContext.build(config);
    }

}
