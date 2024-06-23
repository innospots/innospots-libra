package io.innospots.workflow.runtime.flow.node.logic;

import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.logic.CombineNode;
import io.innospots.workflow.runtime.flow.node.IDataNodeTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/22
 */
public class CombineNodeTest implements IDataNodeTest {

    private String nodekey = "nk123";
    private String nodekey2 = "nk456";


    @Test
    void test(){
        InnospotsIdGenerator.build("127.0.0.1",9808);
        FlowExecution flowExecution = this.buildFlowExecution();
        NodeExecution ne = buildExecution(nodekey,2,false);
        ne.end("ok");
        flowExecution.addNodeExecution(ne);
        NodeExecution ne2 = buildExecution(nodekey2,3,false);
        ne2.end("ok");
        flowExecution.addNodeExecution(ne2);
        this.testExecute(flowExecution);
    }

    @Override
    public NodeInstance build() {
        Map<String,Object> data = new HashMap<>();
        data.put("output_setting", "LAST");
        NodeInstance ni = build(CombineNode.class,data);
        List<String> prevs = new ArrayList<>();
        prevs.add(nodekey2);
        prevs.add(nodekey);
        ni.setPrevNodeKeys(prevs);

        return ni;
    }
}
