package io.innospots.workflow.runtime.engine;

import cn.hutool.core.util.RandomUtil;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.DataFakerUtils;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.loader.FsWorkflowLoader;
import io.innospots.workflow.core.flow.manage.FlowManager;
import io.innospots.workflow.runtime.flow.load.FlowLoadTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/27
 */
class CarrierFlowEngineTest {

    @Test
    void execute() {
        Map<String, Object> input = new HashMap<>(DataFakerUtils.build().sample());
        List<Map<String,Object>> payloads = new ArrayList<>();

        payloads.add(input);
        CarrierFlowEngine carrierFlowEngine = new CarrierFlowEngine(null,null);
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution("flowKey12345",payloads);
        flowExecution.setFlowExecutionId(InnospotsIdGenerator.generateIdStr());
        String uri = "/flow/flow_sample.json";
        Flow flow = FlowLoadTest.buildFlow(uri);
        carrierFlowEngine.execute(flow,flowExecution);
    }

    @Test
    void test2(){
        CarrierFlowEngine carrierFlowEngine = build("flow");
        Map<String, Object> input = new HashMap<>(DataFakerUtils.build().sample());
        List<Map<String,Object>> payloads = new ArrayList<>();
        payloads.add(input);
        FlowExecution flowExecution = buildFlowExecution("flow_sample",payloads);
        carrierFlowEngine.execute(flowExecution);
        outputFlowExecution(flowExecution);
    }

    public void outputFlowExecution(FlowExecution flowExecution) {
        System.out.println(flowExecution.info());
        int i = 1;
        for (Map.Entry<String, NodeExecution> entry : flowExecution.getNodeExecutions().entrySet()) {
            System.out.println(i++ + ": " + entry.getValue().logInfo());
        }
    }

    public FlowExecution buildFlowExecution(String flowKey,List<Map<String,Object>> payloads){
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(flowKey,payloads);
        flowExecution.setFlowExecutionId(InnospotsIdGenerator.generateIdStr());
        return flowExecution;
    }

    public static CarrierFlowEngine build(String workflowPath) {
        InnospotsIdGenerator.build("127.0.0.1",9880);
        CCH.sessionId("sid_"+RandomUtil.randomNumbers(8));
        FsWorkflowLoader workflowLoader = new FsWorkflowLoader(workflowPath+"/");
        FlowManager flowManager = new FlowManager(workflowLoader);
        return new CarrierFlowEngine(flowManager);
    }
}