package io.innospots.workflow.node.app;


import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseAppNode;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.script.PythonNode;
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
class PythonNodeTest extends BaseNodeTest {


    @Test
    void test11() {
        PythonNode baseAppNode = (PythonNode) BaseNodeTest.baseAppNode(this.getClass().getSimpleName());
        NodeExecution nodeExecution = nodeExecution(baseAppNode);
        baseAppNode.invoke(nodeExecution);
        log.info("nodeExecution:{}", nodeExecution.getOutputs());
        for (NodeOutput output : nodeExecution.getOutputs()) {
            System.out.println(output);
        }
    }

    private NodeExecution nodeExecution(BaseAppNode appNode) {
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
        return build(this.getClass().getSimpleName() + ".json");
    }
}