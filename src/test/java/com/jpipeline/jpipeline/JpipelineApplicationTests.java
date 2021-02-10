package com.jpipeline.jpipeline;

import com.jpipeline.jpipeline.entity.SimpleNode;
import com.jpipeline.jpipeline.entity.SimpleNode2;
import com.jpipeline.jpipeline.entity.SimpleNode3;
import com.jpipeline.jpipeline.service.WorkflowContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.UUID;

@SpringBootTest
class JpipelineApplicationTests {

    @Autowired
    private WorkflowContext workflowContext;

    @Test
    void executeWorkflow() {
        System.out.println("\n\n");

        SimpleNode simpleNode1 = new SimpleNode(UUID.randomUUID());
        SimpleNode simpleNode12 = new SimpleNode(UUID.randomUUID());
        simpleNode1.setFirstMessage("My first message! Hello from SimpleNode1!");
        simpleNode12.setFirstMessage("My first message! Hello from SimpleNode12!");

        SimpleNode2 simpleNode2 = new SimpleNode2(UUID.randomUUID());
        SimpleNode3 simpleNode3 = new SimpleNode3(UUID.randomUUID());

        simpleNode1.getWires().add(simpleNode2.getId());
        simpleNode1.getWires().add(simpleNode3.getId());
        simpleNode12.getWires().add(simpleNode3.getId());

        workflowContext.build(Arrays.asList(simpleNode1, simpleNode2, simpleNode3, simpleNode12));

        simpleNode1.pressButton();
        simpleNode12.pressButton();

        System.out.println("\n\n");

        return;
    }




}
