package io.innospots.approve.core.operator;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.approve.core.converter.ApproveExecutionConverter;
import io.innospots.approve.core.dao.ApproveExecutionDao;
import io.innospots.approve.core.entity.ApproveExecutionEntity;
import io.innospots.approve.core.model.ApproveActor;
import io.innospots.approve.core.model.ApproveExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Component
public class ApproveExecutionOperator extends ServiceImpl<ApproveExecutionDao, ApproveExecutionEntity> {


    public ApproveExecution createExecution(NodeExecution nodeExecution, ApproveActor actor){
        ApproveExecutionEntity entity = ApproveExecutionConverter.toEntity(nodeExecution,actor);
        save(entity);
        return ApproveExecutionConverter.INSTANCE.entityToModel(entity);
    }
}
