package io.innospots.libra.kernel.module.task.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.CaseFormat;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.libra.base.task.TaskExecution;
import io.innospots.libra.kernel.module.task.dao.TaskExecutionDao;
import io.innospots.libra.kernel.module.task.entity.TaskExecutionEntity;
import io.innospots.libra.kernel.module.task.converter.TaskExecutionBeanConverter;
import io.innospots.libra.kernel.module.task.model.TaskExecutionForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Slf4j
@Service
public class TaskExecutionOperator extends ServiceImpl<TaskExecutionDao, TaskExecutionEntity> {

    @Transactional(rollbackFor = Exception.class)
    public Boolean createTaskExecution(TaskExecution taskExecution) {
        TaskExecutionBeanConverter mapper = TaskExecutionBeanConverter.INSTANCE;
        TaskExecutionEntity entity = mapper.model2Entity(taskExecution);
        return super.save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTaskExecution(TaskExecution taskExecution) {
        TaskExecutionEntity entity = this.getById(taskExecution.getTaskExecutionId());
        if(entity == null){
            log.error("task execution entity is empty:{} ",taskExecution);
            return false;
        }
        if (taskExecution.getPercent() <= 0 && entity.getPercent() > 0) {
            taskExecution.setPercent(entity.getPercent());
        }
        TaskExecutionBeanConverter.INSTANCE.updateEntity2Model(entity, taskExecution);
        return super.updateById(entity);
    }

    public TaskExecution getTaskExecution(String taskExecutionId) {
        TaskExecutionEntity entity = this.getById(taskExecutionId);
        return TaskExecutionBeanConverter.INSTANCE.entity2Model(entity);
    }

    public TaskExecution getTaskExecutionById(String taskExecutionId) {
        TaskExecution taskExecution = this.getTaskExecution(taskExecutionId);
        taskExecution.setTimeConsume(DateTimeUtils.getDiffDayStr(DateTimeUtils.asDate(taskExecution.getEndTime()), DateTimeUtils.asDate(taskExecution.getStartTime())));
        return taskExecution;
    }

    public PageBody<TaskExecution> pageTaskExecutions(TaskExecutionForm request) {
        QueryWrapper<TaskExecutionEntity> query = new QueryWrapper<>();
        if (StringUtils.isNotBlank(request.getQueryInput())) {
            query.like("TASK_NAME", "%" + request.getQueryInput() + "%");
        }
        if (CollectionUtils.isNotEmpty(request.getAppNames())) {
            query.in("APP_NAME", request.getAppNames());
        }
        if (CollectionUtils.isNotEmpty(request.getTaskCodes())) {
            query.in("EXECUTION_STATUS", request.getTaskCodes());
        }
        if (StringUtils.isNotBlank(request.getStartDate())) {
            query.ge("START_TIME", request.getStartDate() + " 00:00:00");
        }
        if (StringUtils.isNotBlank(request.getEndDate())) {
            query.le("END_TIME", request.getEndDate() + " 23:59:59");
        }
        if (StringUtils.isNotBlank(request.getSort())) {
            query.orderBy(true, request.getAsc(), CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, request.getSort()));
        } else {
            query.orderBy(true, false, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "createdTime"));
        }
        PageBody<TaskExecution> pageBody = new PageBody<>();
        IPage<TaskExecutionEntity> oPage = new Page<>(request.getPage(), request.getSize());
        IPage<TaskExecutionEntity> entityPage = super.page(oPage, query);
        List<TaskExecutionEntity> entities = entityPage.getRecords();
        pageBody.setCurrent(entityPage.getCurrent());
        pageBody.setPageSize(entityPage.getSize());
        pageBody.setTotal(entityPage.getTotal());
        pageBody.setTotalPage(entityPage.getPages());
        List<TaskExecution> taskExecutions = entities.stream().map(TaskExecutionBeanConverter.INSTANCE::entity2Model).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
        this.generateTaskExecutions(taskExecutions);
        pageBody.setList(taskExecutions);
        return pageBody;
    }

    private void generateTaskExecutions(List<TaskExecution> taskExecutions) {
        if (CollectionUtils.isEmpty(taskExecutions)) {
            return;
        }

        for (TaskExecution taskExecution : taskExecutions) {
            taskExecution.setTimeConsume(DateTimeUtils.getDiffDayStr(DateTimeUtils.asDate(taskExecution.getEndTime()), DateTimeUtils.asDate(taskExecution.getStartTime())));
        }
    }
}
