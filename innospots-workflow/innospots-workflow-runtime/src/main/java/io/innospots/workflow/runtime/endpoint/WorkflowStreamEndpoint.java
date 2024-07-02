package io.innospots.workflow.runtime.endpoint;

import io.innospots.base.constant.PathConstant;
import io.innospots.workflow.core.sse.FlowEmitter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Smars
 * @date 2024/6/24
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH + "workflow/execution")
@Tag(name ="workflow execution stream")
public class WorkflowStreamEndpoint {

    @GetMapping("log/{flowExecutionId}")
    public SseEmitter workflowLog(@PathVariable String flowExecutionId,
                                  @RequestParam String streamId){
        return FlowEmitter.createExecutionLogEmitter(flowExecutionId,streamId);
    }

    @GetMapping("response/{flowExecutionId}")
    public SseEmitter workflowResponse(@PathVariable String flowExecutionId,
                                       @RequestParam String streamId){
        return FlowEmitter.createResponseEmitter(flowExecutionId,streamId);
    }
}
