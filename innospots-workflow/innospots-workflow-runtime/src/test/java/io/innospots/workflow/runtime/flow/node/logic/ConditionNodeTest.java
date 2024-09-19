package io.innospots.workflow.runtime.flow.node.logic;

import io.innospots.base.exception.ScriptException;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.logic.ConditionNode;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/23
 */
@Slf4j
public class ConditionNodeTest {

    private static String nodeFileName = "ConditionNodeTest";

    @Test
    public void invoke() throws ScriptException {
        NodeExecution nodeExecution = nodeExecution();
        ConditionNode executor = (ConditionNode) BaseNodeTest.buildExecutor("logic",nodeFileName);

        executor.invoke(nodeExecution);
        log.info("nodeExecution:{}", nodeExecution);

        for (ExecutionOutput output : nodeExecution.getOutputs()) {
            System.out.println(output);
        }

    }


    private NodeExecution nodeExecution() {
        NodeExecution execution = NodeExecution.buildNewNodeExecution("abc", 22L, 1, "432", false);
        Map<String, Object> data = new HashMap<>();
        data.put("f1", "v1");
        data.put("f2", 2);
        data.put("f3", 1.0);

        ExecutionInput input = new ExecutionInput();
        input.addInput(data);
        execution.addInput(input);
        return execution;
    }


    public static NodeInstance build() {
        return BaseNodeTest.build("logic",nodeFileName);
    }

}