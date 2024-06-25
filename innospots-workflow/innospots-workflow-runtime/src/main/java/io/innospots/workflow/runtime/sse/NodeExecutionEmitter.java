package io.innospots.workflow.runtime.sse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.utils.CCH;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/24
 */
@Slf4j
public class NodeExecutionEmitter extends BaseEventEmitter{


    public void sendItem(String eventEmitterId, Map<String,Object> item){
        send(eventEmitterId,"node-execution-item",item);
    }

    public void sendResources(String eventEmitterId, ExecutionResource executionResource){
        send(eventEmitterId,"node-execution-resource",executionResource);
    }

    public void sendLog(String eventEmitterId, Map<String,Object> item){
        send(eventEmitterId,"node-execution-log",item);
    }

}
