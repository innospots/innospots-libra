package io.innospots.workflow.runtime.flow.node;

import io.innospots.base.utils.DataFakerUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/23
 */
public interface IDataNodeTest {


    default void testExecute(){
        testExecute(6);
    }

    default void testExecute(int size){
        BaseNodeExecutor executor = buildNodeExecutor();
        NodeExecution execution = buildExecution(executor.nodeKey(),size,true);
        executor.invoke(execution);
        output(execution);
    }

    default void testExecute(FlowExecution flowExecution){
        BaseNodeExecutor executor = buildNodeExecutor();
        NodeExecution nodeExecution = executor.execute(flowExecution);
        output(nodeExecution);
    }

    default FlowExecution buildFlowExecution(){
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(1l,1);

        return flowExecution;
    }

    default NodeExecution buildExecution(){
        return buildExecution("NodeKey_1" ,2,true);
    }

    default NodeExecution buildExecution(String nodeKey,int size,boolean in){
        NodeExecution execution = NodeExecution.buildNewNodeExecution(nodeKey, 22L, 1, "432", false);
        if(in){
            ExecutionInput input = new ExecutionInput();
            for (int i = 0; i < size; i++) {
                Map<String,Object> m = sampleInput();
                input.addInput(m);
            }
            execution.addInput(input);
        }else{
            ExecutionOutput output = new ExecutionOutput();
            for (int i = 0; i < size; i++) {
                Map<String,Object> m = sampleInput();
                output.addResult(m);
            }
            execution.addOutput(output);
        }

        return execution;
    }

    default void output(NodeExecution nodeExecution){
        BaseNodeTest.output(nodeExecution);
    }

    default BaseNodeExecutor buildNodeExecutor() {
        return BaseNodeTest.buildExecutor(build());
    }

    NodeInstance build();

    default NodeInstance build(Class<?> nodeType, Map<String,Object> data){
        NodeInstance ni = new NodeInstance();
        String name = this.getClass().getSimpleName();
        ni.setName(name);
        ni.setCode(name);
        ni.setNodeKey("NodeKey_" + name);
        ni.setNodeDefinitionId(1);
        ni.setNodeInstanceId(1l);
        ni.setFlowCode("EVENTS");
        ni.setNodeType(nodeType.getName());
        ni.setData(data);
        return ni;
    }

    default Map<String,Object> sampleInput(){
        Map<String,Object> m = new HashMap<>();
        DataFakerUtils df = DataFakerUtils.build();
        m.putAll(df.gen());
        m.putAll(df.genAddress());
        return m;
    }
}
