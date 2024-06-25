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
public class NodeExecutionEmitter {

    private static final long MAX_TIMEOUT_MINUTES = 10;

    /**
     * key : nodeExecutionId
     */
    private Cache<String, List<SseEmitterHolder>> nodeExecutionEmitter = Caffeine.newBuilder()
            .expireAfterAccess(MAX_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .<String, List<SseEmitterHolder>>removalListener((key, list, removalCause) -> {
                log.info("remove node execution emitter:{}",key);
                for (SseEmitterHolder sseEmitterHolder : list) {
                    sseEmitterHolder.complete();
                }
            }).build();


    public void close(String nodeExecutionId,String streamId){
        synchronized (this){
            List<SseEmitterHolder> holders = nodeExecutionEmitter.getIfPresent(nodeExecutionId);
            if(holders != null){
                for (SseEmitterHolder holder : holders) {
                    if(streamId!=null){
                        if(holder.getStreamId().equals(streamId)){
                            holder.complete();
                            holders.remove(holder);
                            break;
                        }
                    }else{
                        holder.complete();
                    }

                }//end for
                if(holders.isEmpty()){
                    nodeExecutionEmitter.invalidate(nodeExecutionId);
                }
            }//end holders
        }//end sync
    }

    public boolean hasExist(String nodeExecutionId, String streamId){
        List<SseEmitterHolder> holders = nodeExecutionEmitter.getIfPresent(nodeExecutionId);
        if(holders == null){
            return false;
        }
        return holders.stream().anyMatch(s -> s.getStreamId().equals(streamId));
    }



    public SseEmitter getOrCreateEmitter(String nodeExecutionId, String streamId) {
        synchronized(this){
            List<SseEmitterHolder> holders = nodeExecutionEmitter.getIfPresent(nodeExecutionId);
            if (holders == null) {
                holders = new ArrayList<>();
                nodeExecutionEmitter.put(nodeExecutionId, holders);
            }
            Integer userId = CCH.userId();
            Optional<SseEmitterHolder> optional = holders.stream().filter(holder -> userId.equals(holder.getUserId()) &&
                    holder.getStreamId().equals(streamId)).findFirst();
            SseEmitterHolder holder;
            if (optional.isEmpty()) {
                holder = SseEmitterHolder.create(streamId, MAX_TIMEOUT_MINUTES * 60 * 1000);
                holders.add(holder);
                holder.onCompletion(()->{
                    close(nodeExecutionId,streamId);
                }).onTimeout(()->{
                    close(nodeExecutionId,streamId);
                });
            } else {
                holder = optional.get();
            }
            return holder.getSseEmitter();
        }
    }

    public void sendItem(String nodeExecutionId, Map<String,Object> item){
        List<SseEmitterHolder> holders = this.nodeExecutionEmitter.getIfPresent(nodeExecutionId);
        if(holders!=null){
            holders.forEach(holder -> {holder.send(nodeExecutionId,item,"nodeExecution-event");});
        }
    }

    public void sendResources(String nodeExecutionId, ExecutionResource executionResource){
        List<SseEmitterHolder> holders = this.nodeExecutionEmitter.getIfPresent(nodeExecutionId);
        if(holders!=null){
            holders.forEach(holder -> {holder.send(executionResource.getResourceId(),executionResource,"nodeExecution-resource");});
        }
    }

    public void sendLog(String nodeExecutionId, Map<String,Object> item){
        List<SseEmitterHolder> holders = this.nodeExecutionEmitter.getIfPresent(nodeExecutionId);
        if(holders!=null){
            holders.forEach(holder -> {holder.send(nodeExecutionId,item,"nodeExecution-log");});
        }
    }

}
