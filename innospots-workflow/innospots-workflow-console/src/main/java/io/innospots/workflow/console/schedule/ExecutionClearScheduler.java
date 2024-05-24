package io.innospots.workflow.console.schedule;

import io.innospots.base.utils.ServiceRoleHolder;
import io.innospots.workflow.console.operator.execution.ExecutionManagerOperator;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.instance.dao.WorkflowInstanceCacheDao;
import io.innospots.workflow.core.instance.entity.WorkflowInstanceEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Smars
 * @date 2023/7/16
 */
@AllArgsConstructor
@Slf4j
@Component
public class ExecutionClearScheduler {


    private final WorkflowInstanceOperator workflowInstanceOperator;

    private final WorkflowInstanceCacheDao workflowInstanceCacheDao;

    private final ExecutionManagerOperator executionManagerOperator;

    private final InnospotsWorkflowProperties innospotsWorkflowProperties;


    @Scheduled(cron = "0 0 4 * * ?")
    private void workflowInstanceCacheCleanTask() {
        try {
            if (ServiceRoleHolder.isCleanTask()) {
                int seconds = innospotsWorkflowProperties.getWorkflowInstanceCacheKeepSeconds();
                LocalDateTime updateTime = LocalDateTime.now();
                updateTime.plusSeconds(seconds);
                int count = workflowInstanceCacheDao.deleteByUpdateTime(updateTime);
                log.info("workflowInstanceCacheCleanTask delete:{} updateTime:{} currTime:{}", count, updateTime, LocalDateTime.now());
            } else {
                log.info("workflowInstanceCacheCleanTask not run!  curr service not leader {} {}", ServiceRoleHolder.isCleanTask(), ServiceRoleHolder.isServiceLeader());
            }
        } catch (Exception e) {
            log.error("workflowInstanceCacheCleanTask error:{}", e.getMessage(), e);
        }

    }


    @Scheduled(cron = "0 10 4 * * ?")
    private void workFlowExecutionLogCleanTask() {
        try {
            if (ServiceRoleHolder.isCleanTask()) {
                int days = innospotsWorkflowProperties.getWorkFlowExecutionKeepDays();

                int keepAmount = innospotsWorkflowProperties.getWorkFlowExecutionKeepAmount();

                LocalDateTime deleteTime = LocalDateTime.now().plusDays(days * -1);
                //只删除workFlow为ONLINE和OFFLINE的记录
                List<WorkflowInstanceEntity> list = workflowInstanceOperator.selectUsedInstance();
                if (list == null || list.isEmpty()) {
                    return;
                }
                for (WorkflowInstanceEntity entity : list) {
                    Long count = executionManagerOperator.getFlowExecutionCount(entity.getWorkflowInstanceId());
                    if (keepAmount < count) {
                        int deleteCount = executionManagerOperator.deleteExecutionLogHis(entity.getWorkflowInstanceId(), deleteTime);
                        log.info("SysOperateLogCleanTask workflowInstanceId:{} delete:{} deleteTime:{} currTime:{}", entity.getWorkflowInstanceId(), deleteCount, deleteTime, LocalDateTime.now());
                    } else {
                        log.info("SysOperateLogCleanTask workflowInstanceId:{} times:{} deleteTime:{} currTime:{}", entity.getWorkflowInstanceId(), count);
                    }
                }
            } else {
                log.info("workflowInstanceCacheCleanTask not run!  curr service not leader {} {}", ServiceRoleHolder.isCleanTask(), ServiceRoleHolder.isServiceLeader());
            }
        } catch (Exception e) {
            log.error("workflowInstanceCacheCleanTask error:{}", e.getMessage(), e);
        }

    }
}
