package io.innospots.libra.kernel.module.task.mapper;

import io.innospots.base.mapper.BaseConvertMapper;
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
public interface TaskExecutionMapper extends BaseConvertMapper {

    TaskExecutionMapper INSTANCE = Mappers.getMapper(TaskExecutionMapper.class);

    TaskExecutionEntity model2Entity(TaskExecution taskExecution);

    TaskExecution entity2Model(TaskExecutionEntity taskExecutionEntity);

    @Mapping(target = "taskExecutionId", ignore = true)
    @Mapping(target = "taskName", ignore = true)
    @Mapping(target = "detailUrl", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    void updateEntity2Model(@MappingTarget TaskExecutionEntity entity, TaskExecution taskExecution);
}
