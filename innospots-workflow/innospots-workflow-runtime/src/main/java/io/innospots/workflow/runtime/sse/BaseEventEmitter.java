package io.innospots.workflow.runtime.sse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.utils.CCH;
import lombok.extern.slf4j.Slf4j;
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
public class BaseEventEmitter {

    private static final long MAX_TIMEOUT_MINUTES = 20;

    /**
     * key : execution id
     */
    protected Cache<String, List<SseEmitterHolder>> eventEmitter = Caffeine.newBuilder()
            .expireAfterAccess(MAX_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .<String, List<SseEmitterHolder>>removalListener((key, list, removalCause) -> {
                log.info("remove event emitter:{}", key);
                for (SseEmitterHolder sseEmitterHolder : list) {
                    sseEmitterHolder.complete();
                }
            }).build();


    public void close(String eventEmitterId, String streamId) {
        synchronized (this) {
            List<SseEmitterHolder> holders = eventEmitter.getIfPresent(eventEmitterId);
            if (holders != null) {
                for (SseEmitterHolder holder : holders) {
                    if (streamId != null) {
                        if (holder.getStreamId().equals(streamId)) {
                            holder.complete();
                            holders.remove(holder);
                            break;
                        }
                    } else {
                        holder.complete();
                    }

                }//end for
                if (holders.isEmpty()) {
                    eventEmitter.invalidate(eventEmitterId);
                }
            }//end holders
        }//end sync
    }

    public boolean hasExist(String eventEmitterId, String streamId) {
        List<SseEmitterHolder> holders = eventEmitter.getIfPresent(eventEmitterId);
        if (holders == null) {
            return false;
        }
        return holders.stream().anyMatch(s -> s.getStreamId().equals(streamId));
    }


    public SseEmitter createEmitter(String eventEmitterId, String streamId) {
        synchronized (this) {
            List<SseEmitterHolder> holders = eventEmitter.getIfPresent(eventEmitterId);
            if (holders == null) {
                holders = new ArrayList<>();
                eventEmitter.put(eventEmitterId, holders);
            }
            Integer userId = CCH.userId();
            Optional<SseEmitterHolder> optional = holders.stream().filter(holder -> userId.equals(holder.getUserId()) &&
                    holder.getStreamId().equals(streamId)).findFirst();
            SseEmitterHolder holder;
            if (optional.isEmpty()) {
                holder = SseEmitterHolder.create(streamId, MAX_TIMEOUT_MINUTES * 60 * 1000);
                holders.add(holder);
                holder.onCompletion(() -> {
                    log.info("complete sse emitter, eventEmitterId: {}, streamId: {}", eventEmitterId, streamId);
                    close(eventEmitterId, streamId);
                }).onTimeout(() -> {
                    log.info("timeout sse emitter, eventEmitterId: {}, streamId: {}", eventEmitterId, streamId);
                    close(eventEmitterId, streamId);
                });
            } else {
                holder = optional.get();
            }
            return holder.getSseEmitter();
        }
    }

    public SseEmitter getEmitter(String nodeExecutionId, String streamId) {
        List<SseEmitterHolder> holders = eventEmitter.getIfPresent(nodeExecutionId);
        if (holders == null) {
            return null;
        }
        Integer userId = CCH.userId();
        Optional<SseEmitterHolder> optional = holders.stream().filter(holder -> userId.equals(holder.getUserId()) &&
                holder.getStreamId().equals(streamId)).findFirst();
        return optional.map(SseEmitterHolder::getSseEmitter).orElse(null);

    }

    public void send(String eventEmitterId,String eventName, Object item) {
        List<SseEmitterHolder> holders = this.eventEmitter.getIfPresent(eventEmitterId);
        if (holders != null) {
            holders.forEach(holder -> {
                holder.send(eventEmitterId, item, eventName);
            });
        }
    }

}
