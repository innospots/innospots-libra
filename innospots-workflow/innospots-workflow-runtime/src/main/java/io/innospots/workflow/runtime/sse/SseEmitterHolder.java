package io.innospots.workflow.runtime.sse;

import io.innospots.base.utils.CCH;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Smars
 * @vesion 2.
 * @date 2024/6/24
 */
@Getter
@Slf4j
public class SseEmitterHolder {

    private SseEmitter sseEmitter;

    private String streamId;

    private Integer userId;

    private int counter;

    public static SseEmitterHolder create(String streamId,long timeout){
        SseEmitterHolder holder = new SseEmitterHolder();
        holder.streamId = streamId;
        holder.sseEmitter = new SseEmitter(timeout);
        holder.userId = CCH.userId();
        return holder;
    }

    public void send(String id,Object data,String eventName){
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .data(data)
                .id(id)
                .name(eventName);
        try {
            sseEmitter.send(event);
            counter++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCompletion(Runnable onComplete){
        sseEmitter.onCompletion(onComplete);
    }

    public void onError(Consumer<Throwable> callback){
        sseEmitter.onError(callback);
    }

    public void onTimeout(Runnable onTimeout){
        sseEmitter.onTimeout(onTimeout);
    }

    public void complete(){
        sseEmitter.complete();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SseEmitterHolder that = (SseEmitterHolder) o;
        return streamId.equals(that.streamId) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, userId);
    }
}
