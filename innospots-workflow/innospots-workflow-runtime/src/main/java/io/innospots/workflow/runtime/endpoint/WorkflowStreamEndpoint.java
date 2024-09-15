package io.innospots.workflow.runtime.endpoint;

import io.innospots.base.constant.PathConstant;
import io.innospots.workflow.core.sse.FlowEmitter;
import io.innospots.workflow.runtime.response.StreamResponseEmitter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Smars
 * @date 2024/6/24
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH + "workflow/stream")
@Tag(name ="workflow execution stream")
public class WorkflowStreamEndpoint {

    private StreamResponseEmitter streamResponseEmitter;

    public WorkflowStreamEndpoint(StreamResponseEmitter streamResponseEmitter) {
        this.streamResponseEmitter = streamResponseEmitter;
    }

    @GetMapping("log/{contextId}")
    public SseEmitter workflowLog(@PathVariable String contextId){
        return streamResponseEmitter.workflowLog(contextId);
    }

    @GetMapping("response/{contextId}")
    public SseEmitter workflowAckMessage(@PathVariable String contextId){
        return streamResponseEmitter.workflowAckMessage(contextId);
    }
}
