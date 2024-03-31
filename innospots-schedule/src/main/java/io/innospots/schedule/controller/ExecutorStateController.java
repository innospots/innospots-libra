package io.innospots.schedule.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.base.quartz.QuartzScheduleManager;
import io.innospots.schedule.launcher.ReadyJobLauncher;
import io.innospots.schedule.model.JobExecution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static io.innospots.base.model.response.InnospotResponse.success;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/3/24
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH +"schedule/executor")
@Tag(name = "schedule executor state")
public class ExecutorStateController {

    private final QuartzScheduleManager quartzScheduleManager;

    private final ReadyJobLauncher readyJobLauncher;

    public ExecutorStateController(QuartzScheduleManager quartzScheduleManager,
                                   ReadyJobLauncher readyJobLauncher) {
        this.quartzScheduleManager = quartzScheduleManager;
        this.readyJobLauncher = readyJobLauncher;
    }

    @GetMapping("quartz-jobs")
    @Operation(summary = "quartz schedule job in the executor")
    public InnospotResponse<List<Map<String, Object>>> scheduleInfo() {
        return success(quartzScheduleManager.schedulerInfo());
    }

    @GetMapping("running-jobs")
    @Operation(summary = "running schedule job in the executor")
    public InnospotResponse<List<JobExecution>> runningJobs() {
        return success(readyJobLauncher.currentCacheExecutions());
    }

}
