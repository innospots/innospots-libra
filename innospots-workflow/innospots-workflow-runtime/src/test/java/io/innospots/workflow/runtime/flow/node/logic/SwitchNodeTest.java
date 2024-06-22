package io.innospots.workflow.runtime.flow.node.logic;

import io.innospots.base.exception.ScriptException;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.logic.SwitchNode;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/26
 */
@Slf4j
public class SwitchNodeTest {

    @Test
    public void invoke() throws ScriptException {

        SwitchNode appNode = (SwitchNode) BaseNodeTest.buildExecutor("logic",SwitchNodeTest.class.getSimpleName());
        NodeExecution nodeExecution = nodeExecution(appNode);
        appNode.invoke(nodeExecution);
        log.info("nodeExecution:{}", nodeExecution);
        BaseNodeTest.output(nodeExecution);
    }

    private NodeExecution nodeExecution(BaseNodeExecutor appNode) {
        NodeExecution execution = NodeExecution.buildNewNodeExecution(appNode.nodeKey(), 22L, 1, "21", false);
        Map<String, Object> data = new HashMap<>();
        data.put("total_level", "1");
        data.put("user_level", "v5");
        data.put("user_age", 10);

        ExecutionInput input = new ExecutionInput();
        input.addInput(data);

        execution.addInput(input);
        return execution;
    }

    public static NodeInstance build() {
        return BaseNodeTest.build("logic",SwitchNodeTest.class.getSimpleName() + ".json");
    }
}