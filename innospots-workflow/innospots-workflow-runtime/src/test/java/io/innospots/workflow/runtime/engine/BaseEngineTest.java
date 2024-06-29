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

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/27
 */
class BaseEngineTest {

    public static void outputFlowExecution(FlowExecution flowExecution) {
        System.out.println(flowExecution.info());
        int i = 1;
        for (Map.Entry<String, NodeExecution> entry : flowExecution.getNodeExecutions().entrySet()) {
            System.out.println(i++ + ": " + entry.getValue().logInfo());
        }
    }

    public static FlowExecution buildFlowExecution(String flowKey,List<Map<String,Object>> payloads){
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