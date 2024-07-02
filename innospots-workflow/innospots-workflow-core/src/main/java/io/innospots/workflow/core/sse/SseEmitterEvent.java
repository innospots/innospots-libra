package io.innospots.workflow.core.sse;

import io.innospots.base.events.EventBody;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/30
 */
public class SseEmitterEvent extends EventBody {

    private String eventEmitterId;
    private String streamId;

    public SseEmitterEvent(String eventEmitterId, String streamId,String eventType) {
        this.eventEmitterId = eventEmitterId;
        this.streamId = streamId;
        this.eventType = eventType;
    }

    public String getEventEmitterId() {
        return eventEmitterId;
    }

    public String getStreamId() {
        return streamId;
    }
}
