package io.innospots.workflow.runtime.engine;

import cn.hutool.core.util.RandomUtil;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.DataFakerUtils;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.flow.Flow;
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
        InnospotsIdGenerator.build("127.0.0.1",9880);
        CCH.sessionId("sid_"+RandomUtil.randomNumbers(8));
        payloads.add(input);
        CarrierFlowEngine carrierFlowEngine = new CarrierFlowEngine(null,null);
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution("flowKey12345",payloads);
        String uri = "/flow/flow_sample.json";
        Flow flow = FlowLoadTest.buildFlow(uri);
        carrierFlowEngine.execute(flow,flowExecution);

    }
}