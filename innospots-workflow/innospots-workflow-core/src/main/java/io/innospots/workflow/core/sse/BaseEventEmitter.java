package io.innospots.workflow.core.sse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.time.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
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
    protected static Cache<String, List<SseEmitterHolder>> eventEmitter = Caffeine.newBuilder()
            .expireAfterAccess(MAX_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .<String, List<SseEmitterHolder>>removalListener((key, list, removalCause) -> {
                log.info("remove event emitter:{}", key);
                for (SseEmitterHolder sseEmitterHolder : list) {
                    sseEmitterHolder.complete();
                }
            }).build();


    public static void close(String eventEmitterId, String streamId) {
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
    }

    public static boolean hasExist(String eventEmitterId, String streamId) {
        List<SseEmitterHolder> holders = eventEmitter.getIfPresent(eventEmitterId);
        if (holders == null) {
            return false;
        }
        return holders.stream().anyMatch(s -> s.getStreamId().equals(streamId));
    }


    public static SseEmitter createEmitter(String eventEmitterId,String eventType, String streamId) {
        synchronized (eventEmitterId) {
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
                EventBusCenter.postSync(new SseEmitterEvent(eventEmitterId, streamId,eventType));
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

    public static SseEmitter getEmitter(String nodeExecutionId, String streamId) {
        List<SseEmitterHolder> holders = eventEmitter.getIfPresent(nodeExecutionId);
        if (holders == null) {
            return null;
        }
        Integer userId = CCH.userId();
        Optional<SseEmitterHolder> optional = holders.stream().filter(holder -> userId.equals(holder.getUserId()) &&
                holder.getStreamId().equals(streamId)).findFirst();
        return optional.map(SseEmitterHolder::getSseEmitter).orElse(null);

    }

    public static void send(String eventEmitterId, String eventName, Object item) {
        List<SseEmitterHolder> holders = eventEmitter.getIfPresent(eventEmitterId);
        if (holders != null) {
            holders.forEach(holder -> {
                holder.send(eventEmitterId, item, eventName);
            });
        }
    }

    public static void sendInfoLog(String eventEmitterId, String eventName, Object message) {
        sendLog(eventEmitterId, eventName, "INFO", message);
    }

    public static void sendErrorLog(String eventEmitterId, String eventName, Object message) {
        sendLog(eventEmitterId, eventName, "ERROR", message);
    }

    public static void sendLog(String eventEmitterId, String eventName, String level, Object message) {
        String sessionId = CCH.sessionId();
        List<SseEmitterHolder> holders = eventEmitter.getIfPresent(eventEmitterId);
        if (holders != null) {
            sendLog(holders, eventEmitterId, eventName, level, message);
        } else if (sessionId != null && !Objects.equals(sessionId, eventEmitterId)) {
            holders = eventEmitter.getIfPresent(eventEmitterId);
            if (holders != null) {
                eventEmitter.put(eventEmitterId, holders);
                sendLog(holders, eventEmitterId, eventName, level, message);
            }
        }
    }

    static void sendLog(List<SseEmitterHolder> holders, String eventEmitterId, String eventName, String level, Object message) {
        if (holders != null) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("log_time", DateTimeUtils.formatLocalDateTime(LocalDateTime.now(), DateTimeUtils.DEFAULT_DATETIME_PATTERN));
            item.put("level", level);
            item.put("message", message);
            holders.forEach(holder -> holder.send(eventEmitterId, item, eventName));
        }
    }

}
