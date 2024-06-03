package io.innospots.workflow.console.controller.task;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.console.model.task.TaskNodeExecution;
import io.innospots.workflow.core.execution.model.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @date 2023/7/16
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/schedule-task/execution")
@ModuleMenu(menuKey = "schedule-task")
@Tag(name = "Schedule task management")
public class ScheduleTaskExecutionController {


    @Operation(summary ="page schedule executions")
    @GetMapping("page/{workflowInstanceId}")
    public InnospotsResponse<PageBody<FlowExecutionBase>> pageExecution(
            @Parameter(name = "page") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(name = "size") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId
    ){

        return null;
    }

    @Operation(summary ="task node execution list")
    @GetMapping("list/flow-execution/{flowExecutionId}")
    public InnospotsResponse<List<TaskNodeExecution>> pageTaskNodeExecution(
            @Parameter(name = "flowExecutionId") @PathVariable String flowExecutionId
    ){
        return null;
    }

    @Operation(summary ="task execution detail")
    @GetMapping("task-execution/{nodeExecutionId}")
    public InnospotsResponse<NodeExecutionDisplay> taskNodeExecution(
            @Parameter(name = "nodeExecutionId") @PathVariable String nodeExecutionId
    ){
        return null;
    }


}
