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
        FlowExecutionEmitter.sendLog(flowExecutionId, message);
    }

    public static void log(String flowExecutionId, Map<String, Object> item) {
        FlowExecutionEmitter.sendLog(flowExecutionId, item);
    }

    public static void log(String flowExecutionId, NodeExecution nodeExecution) {
        FlowExecutionEmitter.sendLog(flowExecutionId, nodeExecution);
    }

    public static void logError(String flowExecutionId, Object message) {
        FlowExecutionEmitter.sendErrorLog(flowExecutionId, message);
    }

    public static void logNodeError(String nodeExecutionId, Object message) {
        NodeExecutionEmitter.sendErrorLog(nodeExecutionId, message);
    }

    public static void logNode(String nodeExecutionId, Object message) {
        NodeExecutionEmitter.sendLog(nodeExecutionId, message);
    }

    public static void logNode(String nodeExecutionId, Map<String, Object> item) {
        NodeExecutionEmitter.sendLog(nodeExecutionId, item);
    }

    public static void nodeItem(String flowExecutionId, String nodeExecutionId, Map<String, Object> item) {
        if (flowExecutionId != null) {
            FlowExecutionEmitter.sendItem(flowExecutionId, item);
        }
        if (nodeExecutionId != null) {
            NodeExecutionEmitter.sendItem(nodeExecutionId, item);
        }
    }

    public static void nodeResource(String flowExecutionId, String nodeExecutionId, ExecutionResource executionResource) {
        NodeExecutionEmitter.sendResources(nodeExecutionId, executionResource);
    }

    public static SseEmitter createExecutionLogEmitter(String flowExecutionId, String streamId) {
        return FlowExecutionEmitter.createEmitter("log-" + flowExecutionId, "flow-execution", streamId);
    }

    public static SseEmitter createResponseEmitter(String flowExecutionId, String streamId) {
        return FlowExecutionEmitter.createEmitter("response-" + flowExecutionId, "flow-response", streamId);
    }
}
