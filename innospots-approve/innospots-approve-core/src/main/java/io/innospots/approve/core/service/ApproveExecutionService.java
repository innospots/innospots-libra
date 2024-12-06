package io.innospots.approve.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.approve.core.converter.ApproveExecutionConverter;
import io.innospots.approve.core.entity.ApproveExecutionEntity;
import io.innospots.approve.core.entity.ApproveFlowInstanceEntity;
import io.innospots.approve.core.model.ApproveExecution;
import io.innospots.approve.core.operator.ApproveExecutionOperator;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ServiceException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.ExecutionStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
@Service
public class ApproveExecutionService {

    private ApproveExecutionOperator executionOperator;

    private ApproveFlowInstanceOperator flowInstanceOperator;

    public ApproveExecutionService(ApproveExecutionOperator executionOperator, ApproveFlowInstanceOperator flowInstanceOperator) {
        this.executionOperator = executionOperator;
        this.flowInstanceOperator = flowInstanceOperator;
    }

    public List<ApproveExecution> listCurrentApproveExecutions(String approveInstanceKey){
        ApproveFlowInstanceEntity flowInstanceEntity = flowInstanceOperator.getById(approveInstanceKey);
        if(flowInstanceEntity == null){
            throw ResourceException.buildNotExistException(this.getClass(), "approve instance is not exist, key: " + approveInstanceKey);
        }
        if(flowInstanceEntity.getFlowExecutionId() == null){
            throw ResourceException.buildAbandonException(this.getClass(), "flow execution is not exist, key: " + approveInstanceKey);
        }
        QueryWrapper<ApproveExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ApproveExecutionEntity::getFlowExecutionId, flowInstanceEntity.getFlowExecutionId());
        return ApproveExecutionConverter.INSTANCE.entitiesToModels(executionOperator.list(qw));
    }

}
