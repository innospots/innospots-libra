package io.innospots.workflow.console.controller.task;

import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.console.model.task.ScheduleTask;
import io.innospots.workflow.core.execution.flow.FlowExecutionBase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("page/${workflowInstanceId}")
    public InnospotResponse<PageBody<FlowExecutionBase>> pageExecution(
            @Parameter(name = "page") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(name = "size") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId
    ){

        return null;
    }



}
