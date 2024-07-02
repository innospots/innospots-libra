package io.innospots.workflow.core.sse;

import io.innospots.base.execution.ExecutionResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/24
 */
@Slf4j
public class NodeExecutionEmitter extends BaseEventEmitter {


    public static void sendItem(String eventEmitterId, Map<String, Object> item) {
        send(eventEmitterId, "node-execution-item", item);
    }

    public static void sendResources(String eventEmitterId, ExecutionResource executionResource) {
        send(eventEmitterId, "node-execution-resource", executionResource);
    }

    public static void sendErrorLog(String flowExecutionId, Object message) {
        sendErrorLog(flowExecutionId, "flow-execution-log", message);
    }

    public static void sendLog(String eventEmitterId, Map<String, Object> item) {
        sendInfoLog(eventEmitterId, "node-execution-log", item);
    }

    public static void sendLog(String eventEmitterId, Object message) {
        sendInfoLog(eventEmitterId, "node-execution-log", message);
    }

}
