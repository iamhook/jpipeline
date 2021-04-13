package com.jpipeline.jpipeline.http;

import com.jpipeline.jpipeline.service.WorkflowService;
import com.jpipeline.common.util.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/node")
public class NodeController {

    @Autowired
    private WorkflowService workflowContext;

    @GetMapping("/{id}/pressButton")
    public void pressButton(@PathVariable UUID id) throws NotFoundException {
        workflowContext.pressButton(id);
    }

}
