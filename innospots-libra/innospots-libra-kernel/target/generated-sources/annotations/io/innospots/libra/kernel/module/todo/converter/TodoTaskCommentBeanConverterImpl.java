package io.innospots.libra.kernel.module.todo.converter;

import io.innospots.libra.kernel.module.todo.entity.TodoTaskCommentEntity;
import io.innospots.libra.kernel.module.todo.model.TodoTaskComment;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-09-22T22:19:20+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_322 (Azul Systems, Inc.)"
)
public class TodoTaskCommentBeanConverterImpl implements TodoTaskCommentBeanConverter {

    @Override
    public TodoTaskComment entity2Model(TodoTaskCommentEntity todoTaskCommentEntity) {
        if ( todoTaskCommentEntity == null ) {
            return null;
        }

        TodoTaskComment todoTaskComment = new TodoTaskComment();

        todoTaskComment.setCommentId( todoTaskCommentEntity.getCommentId() );
        todoTaskComment.setContent( todoTaskCommentEntity.getContent() );
        todoTaskComment.setTaskId( todoTaskCommentEntity.getTaskId() );
        todoTaskComment.setCreatedBy( todoTaskCommentEntity.getCreatedBy() );

        return todoTaskComment;
    }

    @Override
    public TodoTaskCommentEntity model2Entity(TodoTaskComment todoTaskComment) {
        if ( todoTaskComment == null ) {
            return null;
        }

        TodoTaskCommentEntity todoTaskCommentEntity = new TodoTaskCommentEntity();

        todoTaskCommentEntity.setCreatedBy( todoTaskComment.getCreatedBy() );
        todoTaskCommentEntity.setCommentId( todoTaskComment.getCommentId() );
        todoTaskCommentEntity.setContent( todoTaskComment.getContent() );
        todoTaskCommentEntity.setTaskId( todoTaskComment.getTaskId() );

        return todoTaskCommentEntity;
    }
}
