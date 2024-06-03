package io.innospots.workflow.console.controller.task;

import io.innospots.base.enums.DataStatus;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.quartz.ScheduleMode;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.console.model.task.ScheduleTask;
import io.innospots.workflow.console.model.task.ScheduleTimeInfo;
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
@RequestMapping(PATH_ROOT_ADMIN + "workflow/schedule-task")
@ModuleMenu(menuKey = "schedule-task")
@Tag(name = "Schedule task management")
public class ScheduleTaskController {



    @GetMapping("{workflowInstanceId}")
    @Operation(summary = "schedule task info")
    public InnospotsResponse<ScheduleTask> getTask(
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId){

        return null;
    }

    @GetMapping("page")
    @Operation(summary = "page schedule task")
    public InnospotsResponse<PageBody<ScheduleTask>> pageTask(
            @Parameter(name = "page") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(name = "size") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(name = "status") @RequestParam(required = false) DataStatus status,
            @Parameter(name = "appCode") @RequestParam(required = false) String appCode,
            @Parameter(name = "scheduleMode") @RequestParam(required = false) ScheduleMode scheduleMode,
            @Parameter(name = "taskName") @RequestParam(required = false) String taskName){
        return null;
    }

    @PutMapping("status/{workflowInstanceId}")
    @Operation(summary = "online or offline schedule task")
    public InnospotsResponse<ScheduleTask> updateStatus(
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId,
            @Parameter(name = "status") @RequestParam DataStatus status){

        return null;
    }

    @PostMapping("execute/{workflowInstanceId}")
    @Operation(summary = "manual execute task")
    public InnospotsResponse<ScheduleTask> executeTask(
            @Parameter(name = "workflowInstanceId") @PathVariable Long workflowInstanceId){

        return null;
    }


    @PostMapping("schedule-time")
    @Operation(summary = "schedule time")
    public InnospotsResponse<ScheduleTimeInfo> scheduleTime(
            @RequestBody ScheduleTimeInfo scheduleTime
    ){

        return null;
    }

}
