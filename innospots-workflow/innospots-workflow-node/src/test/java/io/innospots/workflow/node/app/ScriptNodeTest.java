package io.innospots.workflow.node.app;

import io.innospots.base.exception.ScriptException;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.script.ScriptNode;
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
public class ScriptNodeTest extends BaseNodeTest {


/*    @Test
    public void test() throws ScriptException {
        NodeInstance instance = build();
        System.out.println(instance);
        GenericExpressionEngine.setPath("target/classes","target/classes");
        GenericExpressionEngine engine = GenericExpressionEngine.build("Test_1");
        System.out.println(GenericExpressionEngine.getClassPath());
//        BaseAppNode baseNode = BaseAppNode.registerToEngine(engine,instance);
        engine.compile();
        System.out.println(engine.dump());
//        baseNode.build("Test_1", instance);
    }*/

    @Test
    public void invoke() throws ScriptException {
        ScriptNode appNode = (ScriptNode) baseAppNode(ScriptNodeTest.class.getSimpleName());
        List<String> nexNodeKeys = new ArrayList<>();
        nexNodeKeys.add("next_key");
        //appNode.setNextNodeKeys(nexNodeKeys);
        NodeExecution nodeExecution = nodeExecution(appNode);
        appNode.invoke(nodeExecution);

        log.info("nodeExecution:{}", nodeExecution);
    }

/*    @Test
    public void invoke(){
        String identifier = "Test_1";
        NodeExecution execution = nodeExecution();
        NodeInstance instance = build();
        BaseAppNode appNode = BaseAppNode.buildAppNode(identifier,instance,null);
        appNode.build(identifier,instance);
        appNode.invoke(nodeExecution());
        //System.out.println(v);
    }*/

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

    public static NodeInstance build() {
        return build(ScriptNodeTest.class.getSimpleName() + ".json");
    }
}