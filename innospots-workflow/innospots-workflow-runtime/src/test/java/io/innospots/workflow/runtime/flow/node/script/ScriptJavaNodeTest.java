package io.innospots.workflow.runtime.flow.node.script;

import io.innospots.base.exception.ScriptException;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/16
 */
@Slf4j
public class ScriptJavaNodeTest {

    private static String nodeFileName = "ScriptJavaNodeSample";

    @Test
    public void invoke() throws ScriptException {
        BaseNodeExecutor executor = BaseNodeTest.buildExecutor("script",nodeFileName);

        List<String> nexNodeKeys = new ArrayList<>();
        nexNodeKeys.add("next_key");
        //appNode.setNextNodeKeys(nexNodeKeys);
        NodeExecution nodeExecution = nodeExecution(executor);
        executor.invoke(nodeExecution);
        log.info("nodeExecution:{}", nodeExecution);
        for (NodeOutput output : nodeExecution.getOutputs()) {
            System.out.println(output.getResults());
        }
    }


    private NodeExecution nodeExecution(BaseNodeExecutor executor) {
        NodeExecution execution = NodeExecution.buildNewNodeExecution(executor.nodeKey(), 22L, 1, "113", true);
        Map<String, Object> data = new HashMap<>();
        data.put("i1", "hello ");
        data.put("i2", " script node");

        ExecutionInput input = new ExecutionInput();
        input.addInput(data);

        execution.addInput(input);
        return execution;
    }

    public static NodeInstance build() {
        return BaseNodeTest.build("script", nodeFileName);
    }
}