package com.jpipeline.entity.function;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class SwitchNode extends Node {

    @NodeProperty
    private List<Condition> condition;

    @NodeProperty
    private String property;

    public SwitchNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {

        int i = 0;
        for (Condition cond : condition) {
            String operator = cond.getOperator();
            String value = cond.getValue();
            if (operator.equals("==") && message.get(property).equals(value)) {
                send(message, i);
            }
            if (operator.equals("!=") && !message.get(property).equals(value)) {
                send(message, i);
            }

            i++;
        }
    }

    @Getter @Setter @NoArgsConstructor
    private static class Condition {
        private String operator;
        private String value;
    }

}
