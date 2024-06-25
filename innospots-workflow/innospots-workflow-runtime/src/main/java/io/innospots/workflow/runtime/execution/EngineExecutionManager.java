package io.innospots.workflow.runtime.execution;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @date 2024/6/24
 */
public class EngineExecutionManager {

    protected Cache<String, FlowExecution> flowExecutionCache = Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    private Cache<String,String> streamIdCache = Caffeine.newBuilder().expireAfterAccess(5,TimeUnit.MINUTES).build();

    public void cache(FlowExecution flowExecution){
        flowExecutionCache.put(flowExecution.getFlowExecutionId(),flowExecution);
    }

    public Flux<ServerSentEvent<Map<String,Object>>> nodeExecutionLog(String streamId,String flowExecutionId){
        FlowExecution flow = flowExecutionCache.getIfPresent(flowExecutionId);
        if(flow == null){
            return Flux.empty();
        }
        String currentNodeExecutionId = streamIdCache.getIfPresent(streamId);

        return null;
//        return Flux.interval(Duration.ofMillis(500)).map(m->);

    }

}
