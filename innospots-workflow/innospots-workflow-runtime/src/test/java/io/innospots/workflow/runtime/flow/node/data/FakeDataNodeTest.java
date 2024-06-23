package io.innospots.workflow.runtime.flow.node.data;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.FakerExpression;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.node.app.data.FakeDataNode;
import io.innospots.workflow.runtime.flow.node.IDataNodeTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/23
 */
public class FakeDataNodeTest implements IDataNodeTest {

    @Test
    void test1(){
        BaseNodeExecutor executor = buildNodeExecutor();
        NodeExecution nodeExecution = buildExecution();
        executor.invoke(nodeExecution);
        output(nodeExecution);
    }

    @Override
    public NodeExecution buildExecution() {
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

    @Override
    public NodeInstance build() {
        NodeInstance ni = new NodeInstance();
        ni.setName(FakeDataNodeTest.class.getSimpleName());
        ni.setCode(FakeDataNodeTest.class.getSimpleName());
        ni.setNodeKey("NodeKey_" + FakeDataNodeTest.class.getSimpleName());
        ni.setNodeDefinitionId(1);
        ni.setNodeInstanceId(1l);
        ni.setFlowCode("EVENTS");
        ni.setNodeType(FakeDataNode.class.getName());
        Map<String,Object> data = new HashMap<>();
        data.put(FakeDataNode.FIELD_ITEM_SIZE,10);
        data.put(FakeDataNode.FIELD_FAKE_DATA, JSONUtils.toJsonString(fakeData()));
        ni.setData(data);
        return ni;
    }

    private Map<String,String> fakeData(){
        Map<String,String> m = new HashMap<>();
        m.put("birthday", FakerExpression.expBirthday("yyyy-MM-dd",0,50));
        m.put("numbers", FakerExpression.expNumber("abc##"));
        m.put("letter", FakerExpression.expLetter("??abc??"));
        m.put("rex", FakerExpression.expRegexify("[a-z0-9]"));
        m.put("date_future", FakerExpression.expFutureDate("hh:mm:ss", TimeUnit.MINUTES,"30"));
        m.put("options",FakerExpression.expOptions("ab","cd","ef","g","h"));
        m.put("country",FakerExpression.ADDRESS_COUNTRY);
        m.put("dish",FakerExpression.FOOD_DISH);
        m.put("fullName",FakerExpression.NAME_FULLNAME);
        return m;
    }
}
