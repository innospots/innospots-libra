package io.innospots.workflow.node.app;


import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import org.junit.jupiter.api.Test;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/8/30
 */
public class DerivedVariableNodeTest extends BaseNodeTest {

    @Test
    public void buildExpression() {

        NodeExecution nodeExecution = nodeExecution();
        BaseNodeExecutor appNode = baseAppNode(DerivedVariableNodeTest.class.getSimpleName());

        appNode.invoke(nodeExecution);
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