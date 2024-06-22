package io.innospots.workflow.runtime.flow.node.compute;


import io.innospots.base.function.definition.loader.IFunctionLoader;
import io.innospots.base.function.definition.model.FunctionDefinition;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/8/30
 */
@ExtendWith(MockitoExtension.class)
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
        inputs.put("user_number", 991.112);
        inputs.put("pos", 200);
        inputs.put("user_age", 30);
        ExecutionInput executionInput = new ExecutionInput("is_key");
        executionInput.addInput(inputs);
        execution.addInput(executionInput);
        return execution;
    }


}