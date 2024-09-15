package io.innospots.workflow.runtime.response;

import cn.hutool.core.thread.ThreadUtil;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.AsyncExecutors;
import io.innospots.workflow.core.execution.model.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.execution.model.node.OutputDisplay;
import io.innospots.workflow.core.execution.reader.FlowExecutionReader;
import io.innospots.workflow.core.execution.reader.NodeExecutionReader;
import io.innospots.workflow.core.sse.FlowEmitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/14
 */
@Slf4j
public class StreamResponseEmitter {

    private final NodeExecutionReader nodeExecutionReader;

    private final FlowExecutionReader flowExecutionReader;

    public StreamResponseEmitter(NodeExecutionReader nodeExecutionReader, FlowExecutionReader flowExecutionReader) {
        this.nodeExecutionReader = nodeExecutionReader;
        this.flowExecutionReader = flowExecutionReader;
    }

    public SseEmitter workflowLog(String contextId) {
        SseEmitter sseEmitter = FlowEmitter.getExecutionLogEmitter(contextId);
        if (sseEmitter == null) {
            Map<String, NodeExecutionDisplay> nodeExecutionDisplays = nodeExecutionReader.readExecutionByFlowExecutionId(contextId);
            if (MapUtils.isEmpty(nodeExecutionDisplays)) {
                throw ResourceException.buildNotExistException(this.getClass(), "flow execution not exist", contextId);
            }
            sseEmitter = FlowEmitter.createExecutionLogEmitter(contextId);
            asyncWriteLogToEmitter(contextId, nodeExecutionDisplays);
        }
        return sseEmitter;
    }

    private void asyncWriteLogToEmitter(String contextId, Map<String, NodeExecutionDisplay> nodeExecutionDisplays) {
        AsyncExecutors.execute(() -> {
            ThreadUtil.sleep(1000);
            List<NodeExecutionDisplay> displays = nodeExecutionDisplays.values().stream().sorted().toList();
            for (NodeExecutionDisplay display : displays) {
                FlowEmitter.log(contextId, display.getLogs());
            }
            ThreadUtil.sleep(200);

            FlowEmitter.closeLog(contextId);
        });
    }

    public SseEmitter workflowAckMessage(String contextId) {
        SseEmitter sseEmitter = FlowEmitter.getResponseEmitter(contextId);
        if (sseEmitter == null) {
            sseEmitter = FlowEmitter.createResponseEmitter(contextId);
        }
        FlowExecutionBase flowExecution = flowExecutionReader.getFlowExecutionById(contextId);

        if (flowExecution == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "flow execution not exist", contextId);
        }
        asyncWriteAckMessageToEmitter(contextId, flowExecution);

        return sseEmitter;
    }

    private void asyncWriteAckMessageToEmitter(String contextId, FlowExecutionBase flowExecution) {
        AsyncExecutors.execute(() -> {
            ThreadUtil.sleep(1000);

            List<NodeExecution> nodeExecutions = flowExecution.getNodeExecutions().values()
                    .stream().sorted(Comparator.comparing(NodeExecution::getSequenceNumber)).toList();
            for (NodeExecution nodeExecution : nodeExecutions) {
                NodeExecutionDisplay display = nodeExecutionReader.findNodeExecution(nodeExecution.getNodeExecutionId(), 1, 50);
                if (CollectionUtils.isEmpty(display.getOutputs())) {
                    continue;
                }
                for (OutputDisplay outputDisplay : display.getOutputs()) {
                    if (outputDisplay.getResults().getList() != null) {
                        outputDisplay.getResults().getList().forEach(item -> {
                            FlowEmitter.item(flowExecution.getFlowExecutionId(), item);
                        });
                    }
                    if(outputDisplay.getResources()!=null){
                        outputDisplay.getResources().forEach((k,list)->{
                            for (ExecutionResource resource : list) {
                                FlowEmitter.resource(flowExecution.getFlowExecutionId(), resource);
                            }
                        });
                    }

                }
            }

            ThreadUtil.sleep(200);
            FlowEmitter.closeResponse(contextId);
        });
    }

}
