package io.innospots.workflow.runtime.sse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/25
 */
@Slf4j
public class FlowExecutionEmitter extends BaseEventEmitter{


    public static void sendLog(String flowExecutionId, Object message){
        sendInfoLog(flowExecutionId,"flow-execution-log",message);
    }

    public static void sendLog(String flowExecutionId, String message){
        sendInfoLog(flowExecutionId,"flow-execution-log",message);
    }

    public static void sendErrorLog(String flowExecutionId, Object message){
        sendErrorLog(flowExecutionId,"flow-execution-log",message);
    }

    public static void sendLog(String flowExecutionId, NodeExecution nodeExecution){
        sendInfoLog(flowExecutionId,"flow-node-execution-log",nodeExecution.logInfo());
    }

    public static void sendItem(String flowExecutionId,Map<String,Object> item){
        send(flowExecutionId,"flow-execution-item",item);
    }
}
