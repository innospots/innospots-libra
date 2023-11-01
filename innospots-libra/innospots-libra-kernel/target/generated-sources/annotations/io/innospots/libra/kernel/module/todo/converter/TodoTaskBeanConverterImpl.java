package io.innospots.libra.kernel.module.todo.converter;

import io.innospots.libra.kernel.module.todo.entity.TodoTaskEntity;
import io.innospots.libra.kernel.module.todo.model.TodoTask;
import java.time.LocalDateTime;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-09-22T22:19:20+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_322 (Azul Systems, Inc.)"
)
public class TodoTaskBeanConverterImpl implements TodoTaskBeanConverter {

    @Override
    public TodoTaskEntity model2Entity(TodoTask todoTask) {
        if ( todoTask == null ) {
            return null;
        }

        TodoTaskEntity todoTaskEntity = new TodoTaskEntity();

        if ( todoTask.getCreatedTime() != null ) {
            todoTaskEntity.setCreatedTime( LocalDateTime.parse( todoTask.getCreatedTime() ) );
        }
        if ( todoTask.getUpdatedTime() != null ) {
            todoTaskEntity.setUpdatedTime( LocalDateTime.parse( todoTask.getUpdatedTime() ) );
        }
        todoTaskEntity.setCreatedBy( todoTask.getCreatedBy() );
        todoTaskEntity.setTaskId( todoTask.getTaskId() );
        todoTaskEntity.setTaskName( todoTask.getTaskName() );
        todoTaskEntity.setDescription( todoTask.getDescription() );
        todoTaskEntity.setPrincipalUserId( todoTask.getPrincipalUserId() );
        todoTaskEntity.setTaskStatus( todoTask.getTaskStatus() );
        todoTaskEntity.setTaskPriority( todoTask.getTaskPriority() );
        todoTaskEntity.setStartDate( todoTask.getStartDate() );
        todoTaskEntity.setEndDate( todoTask.getEndDate() );

        return todoTaskEntity;
    }

    @Override
    public TodoTask entity2Model(TodoTaskEntity todoTaskEntity) {
        if ( todoTaskEntity == null ) {
            return null;
        }

        TodoTask todoTask = new TodoTask();

        todoTask.setTaskId( todoTaskEntity.getTaskId() );
        todoTask.setTaskName( todoTaskEntity.getTaskName() );
        todoTask.setDescription( todoTaskEntity.getDescription() );
        todoTask.setPrincipalUserId( todoTaskEntity.getPrincipalUserId() );
        todoTask.setTaskStatus( todoTaskEntity.getTaskStatus() );
        todoTask.setTaskPriority( todoTaskEntity.getTaskPriority() );
        todoTask.setStartDate( todoTaskEntity.getStartDate() );
        todoTask.setEndDate( todoTaskEntity.getEndDate() );
        todoTask.setCreatedBy( todoTaskEntity.getCreatedBy() );

        todoTask.setCreatedTime( timeToString(todoTaskEntity.getCreatedTime()) );
        todoTask.setUpdatedTime( timeToString(todoTaskEntity.getUpdatedTime()) );

        return todoTask;
    }

    @Override
    public void updateEntity2Model(TodoTaskEntity entity, TodoTask task) {
        if ( task == null ) {
            return;
        }

        entity.setCreatedBy( task.getCreatedBy() );
        entity.setTaskName( task.getTaskName() );
        entity.setDescription( task.getDescription() );
        entity.setPrincipalUserId( task.getPrincipalUserId() );
        entity.setTaskStatus( task.getTaskStatus() );
        entity.setTaskPriority( task.getTaskPriority() );
        entity.setStartDate( task.getStartDate() );
        entity.setEndDate( task.getEndDate() );
    }
}
