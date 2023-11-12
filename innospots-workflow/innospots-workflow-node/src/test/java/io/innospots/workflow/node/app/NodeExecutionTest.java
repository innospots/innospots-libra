package io.innospots.workflow.node.app;

import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/18
 */
public class NodeExecutionTest {


    public static NodeExecution build(String nodeKey){
        NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution(
                nodeKey,1L,1,"fk"
                ,false
        );

        return nodeExecution;
    }

    @Test
    void fillExecutionContext() {
        ExecutionInput input = new ExecutionInput();
        Map<String, Object> m = new HashMap<>();
        m.put("abc", "{abc:\"dd0a\"dfas\"dddd\"}");
        input.addInput(m);
        System.out.println(m);
        List<ExecutionInput> inputs = new ArrayList<>();
        inputs.add(input);
        String json = JSONUtils.toJsonString(inputs);
        System.out.println(json);
        inputs = JSONUtils.toList(json, ExecutionInput.class);
        System.out.println(inputs);


    }
}