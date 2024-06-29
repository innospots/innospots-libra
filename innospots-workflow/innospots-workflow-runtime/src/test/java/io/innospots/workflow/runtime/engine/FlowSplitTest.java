package io.innospots.workflow.runtime.engine;

import io.innospots.base.utils.DataFakerUtils;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.innospots.workflow.runtime.engine.BaseEngineTest.*;


/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/27
 */
class FlowSplitTest {

    @Test
    void test() {
        CarrierFlowEngine carrierFlowEngine = build("flow");
        Map<String, Object> input = new HashMap<>(DataFakerUtils.build().sample());
        List<Map<String, Object>> payloads = new ArrayList<>();
        payloads.add(input);
        FlowExecution flowExecution = buildFlowExecution("flow_split", payloads);
        carrierFlowEngine.execute(flowExecution);
        outputFlowExecution(flowExecution);
    }

}