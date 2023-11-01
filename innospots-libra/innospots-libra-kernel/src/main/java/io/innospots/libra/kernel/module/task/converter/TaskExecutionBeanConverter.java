package io.innospots.libra.kernel.module.task.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.base.task.TaskExecution;
import io.innospots.libra.kernel.module.task.entity.TaskExecutionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @date 2023/8/7
 */
@Mapper
public interface TaskExecutionBeanConverter extends BaseBeanConverter <TaskExecution,TaskExecutionEntity>{

    TaskExecutionBeanConverter INSTANCE = Mappers.getMapper(TaskExecutionBeanConverter.class);

    @Mapping(target = "paramContext", expression = "java(mapToJsonStr(taskExecution.getParamContext()))")
    TaskExecutionEntity model2Entity(TaskExecution taskExecution);

    @Mapping(target = "paramContext", expression = "java(jsonStrToMap(taskExecutionEntity.getParamContext()))")
    TaskExecution entity2Model(TaskExecutionEntity taskExecutionEntity);

    @Mapping(target = "taskExecutionId", ignore = true)
    @Mapping(target = "taskName", ignore = true)
    @Mapping(target = "paramContext", ignore = true)
    @Mapping(target = "detailUrl", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    void updateEntity2Model(@MappingTarget TaskExecutionEntity entity, TaskExecution taskExecution);
}
