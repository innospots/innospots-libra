package io.innospots.workflow.node.app.llm;

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/18
 */
@Slf4j
public class LlmNode extends AliAiNode {

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = this.buildOutput(nodeExecution);
        BaseRequest llmRequest = buildRequest(nodeExecution);
        switch (executeMode) {
            case STREAM:
                StringBuilder content = new StringBuilder();
                Flux fluxStream = llmDataOperator.executeStream(llmRequest);
                fluxStream.doOnNext(b -> {
                    Map<String, Object> m = (Map<String, Object>) b;
                    flowLogger.item(nodeExecution.getFlowExecutionId(), m);
                    content.append(m.get("content"));
                }).blockLast();
                processOutput(nodeExecution, content.toString(), nodeOutput);
                break;
            case SYNC:
                DataBody dataBody = llmDataOperator.execute(llmRequest);
                Object resp = dataBody.getBody();
                processOutput(nodeExecution, resp, nodeOutput);
                break;
            default:
        }
    }

}
