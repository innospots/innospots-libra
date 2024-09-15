package io.innospots.workflow.core.sse;

import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/26
 */
public class FlowEmitter {


    public static void log(String flowExecutionId, Object message) {
        FlowExecutionEmitter.sendLog("log-" + flowExecutionId, message);
    }

    public static void log(String flowExecutionId, Map<String, Object> item) {
        FlowExecutionEmitter.sendLog("log-" + flowExecutionId, item);
    }

    public static void log(String flowExecutionId, NodeExecution nodeExecution) {
        FlowExecutionEmitter.sendLog("log-" + flowExecutionId, nodeExecution);
    }

    public static void logError(String flowExecutionId, Object message) {
        FlowExecutionEmitter.sendErrorLog("log-" + flowExecutionId, message);
    }

    public static void item(String flowExecutionId, Map<String, Object> item) {
        if (flowExecutionId != null) {
            FlowExecutionEmitter.sendItem("response-" + flowExecutionId, item);
        }
    }

    public static void resource(String flowExecutionId, ExecutionResource executionResource) {
        FlowExecutionEmitter.sendResource("response-" + flowExecutionId, executionResource.toMetaInfo());
    }

    public static SseEmitter createExecutionLogEmitter(String flowExecutionId, String streamId) {
        return FlowExecutionEmitter.createEmitter("log-" + flowExecutionId, "flow-execution", streamId);
    }

    public static SseEmitter createResponseEmitter(String flowExecutionId, String streamId) {
        return FlowExecutionEmitter.createEmitter("response-" + flowExecutionId, "flow-response", streamId);
    }

    public static SseEmitter createExecutionLogEmitter(String flowExecutionId) {
        return createExecutionLogEmitter("log-" + flowExecutionId, flowExecutionId);
    }

    public static SseEmitter createResponseEmitter(String flowExecutionId) {
        return createResponseEmitter("response-" + flowExecutionId, flowExecutionId);
    }

    public static SseEmitter getExecutionLogEmitter(String flowExecutionId, String streamId) {
        return FlowExecutionEmitter.getEmitter("log-" + flowExecutionId, streamId);
    }

    public static SseEmitter getResponseEmitter(String flowExecutionId, String streamId) {
        return FlowExecutionEmitter.getEmitter("response-" + flowExecutionId, streamId);
    }

    public static SseEmitter getExecutionLogEmitter(String flowExecutionId) {
        return getExecutionLogEmitter("log-" + flowExecutionId, flowExecutionId);
    }

    public static SseEmitter getResponseEmitter(String flowExecutionId) {
        return getResponseEmitter("response-" + flowExecutionId, flowExecutionId);
    }

    public static void closeLog(String flowExecutionId) {
        FlowExecutionEmitter.close("log-" + flowExecutionId);
    }

    public static void closeResponse(String flowExecutionId) {
        FlowExecutionEmitter.close("response-" + flowExecutionId);
    }
}
