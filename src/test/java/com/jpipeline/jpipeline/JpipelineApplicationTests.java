package com.jpipeline.jpipeline;

import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.entity.SimpleNode;
import com.jpipeline.jpipeline.entity.SimpleNode2;
import com.jpipeline.jpipeline.service.WorkflowContext;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class JpipelineApplicationTests {

    @Autowired
    private WorkflowContext workflowContext;

    @Test
    void executeWorkflow() {

        SimpleNode simpleNode = new SimpleNode(UUID.randomUUID());
        SimpleNode2 simpleNode2 = new SimpleNode2(UUID.randomUUID());

        simpleNode.getWires().add(simpleNode2.getId());

        workflowContext.build(Arrays.asList(simpleNode, simpleNode2));

        simpleNode.pressButton();

        return;
    }




}
