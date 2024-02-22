package io.innospots.schedule.watcher;

import io.innospots.base.watcher.AbstractWatcher;
import io.innospots.schedule.explore.JobExecutionExplorer;
import io.innospots.schedule.launcher.ReadyJobLauncher;
import io.innospots.schedule.utils.ScheduleUtils;

/**
 * @author Smars
 * @date 2024/2/22
 */
public class RunningExecutionWatcher extends AbstractWatcher {

    private ReadyJobLauncher readyJobLauncher;

    private JobExecutionExplorer jobExecutionExplorer;

    public RunningExecutionWatcher(ReadyJobLauncher readyJobLauncher,
                                   JobExecutionExplorer jobExecutionExplorer) {
        this.readyJobLauncher = readyJobLauncher;
        this.jobExecutionExplorer = jobExecutionExplorer;
    }

    @Override
    public int execute() {
        if (ScheduleUtils.isExecutorLeader()) {
            //only execute in the leader service
            jobExecutionExplorer.updateStoppingTimeoutExecutions();
        }
        int runningJobCount = readyJobLauncher.checkRunningJobs();
        return runningJobCount == 0 ? checkIntervalSecond : 2;
    }

}
