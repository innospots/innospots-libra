package io.innospots.workflow.runtime.flow.node.compute;


import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import org.junit.jupiter.api.Test;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/8/30
 */
public class DerivedVariableNodeTest {

    private static String nodeFileName = "DerivedVariableNodeTest";

    @Test
    public void buildExpression() {
        NodeExecution nodeExecution = nodeExecution();
        BaseNodeExecutor nodeExecutor = BaseNodeTest.buildExecutor("compute",nodeFileName);
        nodeExecutor.invoke(nodeExecution);
        for (NodeOutput output : nodeExecution.getOutputs()) {
            System.out.println(output);
        }
        //System.out.println(v);
    }


    private NodeExecution nodeExecution() {
        NodeExecution execution = NodeExecution.buildNewNodeExecution("abc", 22L, 1, "432", false);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("user_number", "112201");
        inputs.put("pos", 200);
        inputs.put("user_age", 30);
        //execution.setInput(inputs);
        return execution;
    }


}