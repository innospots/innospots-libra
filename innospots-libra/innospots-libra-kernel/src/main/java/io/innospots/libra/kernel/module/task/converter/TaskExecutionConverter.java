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
public interface TaskExecutionConverter extends BaseBeanConverter<TaskExecution,TaskExecutionEntity>{

    TaskExecutionConverter INSTANCE = Mappers.getMapper(TaskExecutionConverter.class);


    @Mapping(target = "taskExecutionId", ignore = true)
    @Mapping(target = "taskName", ignore = true)
    @Mapping(target = "paramContext", ignore = true)
    @Mapping(target = "detailUrl", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    void updateEntity2Model(@MappingTarget TaskExecutionEntity entity, TaskExecution taskExecution);
}
