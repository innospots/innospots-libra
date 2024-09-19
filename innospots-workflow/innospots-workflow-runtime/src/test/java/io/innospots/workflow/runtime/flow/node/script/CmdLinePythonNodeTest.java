package io.innospots.workflow.runtime.flow.node.script;


import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.script.CmdLinePythonNode;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/25
 */
@Slf4j
class CmdLinePythonNodeTest {



    @Test
    void test11() {
        CmdLinePythonNode executor = (CmdLinePythonNode) BaseNodeTest.buildExecutor("script",this.getClass().getSimpleName());
        NodeExecution nodeExecution = nodeExecution(executor);
        executor.invoke(nodeExecution);
        log.info("nodeExecution:{}", nodeExecution.getOutputs());
        for (ExecutionOutput output : nodeExecution.getOutputs()) {
            System.out.println(output);
        }
    }

    private NodeExecution nodeExecution(BaseNodeExecutor appNode) {
        NodeExecution execution = NodeExecution.buildNewNodeExecution(appNode.nodeKey(), 22L, 1, "113", true);
        Map<String, Object> data = new HashMap<>();
        data.put("i1", "hello ");
        data.put("i2", " script node");

        ExecutionInput input = new ExecutionInput();
        input.addInput(data);

        execution.addInput(input);
        return execution;
    }


    public NodeInstance build() {
        return BaseNodeTest.build("script",this.getClass().getSimpleName());
    }
}