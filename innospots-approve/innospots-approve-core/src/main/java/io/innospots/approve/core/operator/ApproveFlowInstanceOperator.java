package io.innospots.approve.core.operator;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.approve.core.converter.ApproveFlowInstanceConverter;
import io.innospots.approve.core.dao.ApproveFlowInstanceDao;
import io.innospots.approve.core.entity.ApproveFlowInstanceEntity;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveFlowInstanceBase;
import io.innospots.approve.core.model.ApproveForm;
import io.innospots.approve.core.model.ApproveRequest;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.CCH;
import io.innospots.base.utils.time.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/22
 */
@Slf4j
@Component
public class ApproveFlowInstanceOperator extends ServiceImpl<ApproveFlowInstanceDao, ApproveFlowInstanceEntity> {

    public ApproveFlowInstance start(String approveInstanceKey) {
        ApproveFlowInstance instance = findOne(approveInstanceKey);
        if(instance.getApproveStatus() != ApproveStatus.DRAFT){
            throw ResourceException.buildStatusException(this.getClass(), "the resource status is " +
                    instance.getApproveStatus() + ", can't be start", approveInstanceKey);
        }
        UpdateWrapper<ApproveFlowInstanceEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ApproveFlowInstanceEntity::getApproveStatus, ApproveStatus.STARTING.name())
                .set(ApproveFlowInstanceEntity::getStartTime, LocalDateTime.now())
                .set(ApproveFlowInstanceEntity::getFlowExecutionId,null)
                .eq(ApproveFlowInstanceEntity::getApproveInstanceKey, approveInstanceKey);
        log.info("update approve instance status to starting: {}", approveInstanceKey);
        boolean b = this.update(uw);
        return ApproveFlowInstanceConverter.INSTANCE.entityToModel(this.getById(approveInstanceKey));
    }

    public ApproveFlowInstance save(ApproveForm approveForm) {
        if (approveForm.getApproveInstanceKey() == null) {
            Date now = new Date();
            String prefix = approveForm.getFlowKey().toUpperCase() + DateTimeUtils.formatDate(now, "yyMMdd");
            String key = null;
            long c = 0;
            do {
                key = prefix + String.format("%04d", CCH.userId()) + RandomUtil.randomNumbers(4);
                QueryWrapper<ApproveFlowInstanceEntity> qw = new QueryWrapper<>();
                qw.lambda().eq(ApproveFlowInstanceEntity::getApproveInstanceKey, key);
                c = this.count(qw);
            } while (c != 0);
            approveForm.setApproveInstanceKey(key);
        }
        ApproveFlowInstanceEntity entity = ApproveFlowInstanceConverter.INSTANCE.formToEntity(approveForm);
        this.saveOrUpdate(entity);
        return ApproveFlowInstanceConverter.INSTANCE.entityToModel(entity);
    }

    public boolean revoke(String approveInstanceKey, String message) {
        ApproveFlowInstance instance = findOne(approveInstanceKey);
        if (instance.getApproveStatus() != ApproveStatus.STARTING) {
            throw ResourceException.buildStatusException(this.getClass(), "the resource status is " +
                    instance.getApproveStatus() +
                    ", which only starting status that can be revoked", approveInstanceKey);
        }
        return updateApproveStatus(approveInstanceKey, ApproveStatus.DRAFT, message);
    }

    public boolean updateProcessStatus(String approveInstanceKey) {
        ApproveFlowInstance instance = findOne(approveInstanceKey);
        if (instance.getApproveStatus() == ApproveStatus.STARTING) {
            return updateApproveStatus(approveInstanceKey, ApproveStatus.PROCESSING, null);
        }
        return false;
    }

    public boolean remove(String approveInstanceKey) {
        ApproveFlowInstance instance = findOne(approveInstanceKey);
        if (instance.getApproveStatus() == ApproveStatus.STARTING ||
                instance.getApproveStatus() == ApproveStatus.PROCESSING ||
                instance.getApproveStatus() == ApproveStatus.APPROVED
        ) {
            throw ResourceException.buildStatusException(this.getClass(), "the resource status is " +
                    instance.getApproveStatus() + ", can't be removed", approveInstanceKey);
        }
        return updateApproveStatus(approveInstanceKey, ApproveStatus.REMOVED, null);
    }

    public boolean approve(String approveInstanceKey, String message) {
        ApproveFlowInstance instance = findOne(approveInstanceKey);
        if (instance.getApproveStatus() != ApproveStatus.PROCESSING &&
                instance.getApproveStatus() != ApproveStatus.STARTING
        ) {
            throw ResourceException.buildStatusException(this.getClass(), "the resource status is " +
                    instance.getApproveStatus() + ", can't be approved", approveInstanceKey);
        }
        return updateApproveStatus(approveInstanceKey, ApproveStatus.APPROVED, message);
    }

    public boolean reject(String approveInstanceKey, String message) {
        ApproveFlowInstance instance = findOne(approveInstanceKey);
        if (instance.getApproveStatus() != ApproveStatus.PROCESSING &&
                instance.getApproveStatus() != ApproveStatus.STARTING
        ) {
            throw ResourceException.buildStatusException(this.getClass(), "the resource status is " +
                    instance.getApproveStatus() + ", can't be rejected", approveInstanceKey);
        }
        return updateApproveStatus(approveInstanceKey, ApproveStatus.REJECTED, message);
    }

    public boolean stop(String approveInstanceKey, String message) {
        ApproveFlowInstance instance = findOne(approveInstanceKey);
        if (instance.getApproveStatus() != ApproveStatus.PROCESSING &&
                instance.getApproveStatus() != ApproveStatus.STARTING
        ) {
            throw ResourceException.buildStatusException(this.getClass(), "the resource status is " +
                    instance.getApproveStatus() + ", can't be stopped", approveInstanceKey);
        }
        return updateApproveStatus(approveInstanceKey, ApproveStatus.STOPPED, message);
    }

    public ApproveFlowInstance findOne(String approveInstanceKey) {
        QueryWrapper<ApproveFlowInstanceEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ApproveFlowInstanceEntity::getApproveInstanceKey, approveInstanceKey);
        ApproveFlowInstanceEntity entity = this.getOne(qw);
        if (entity == null) {
            throw ResourceException.buildNotExistException(this.getClass(), approveInstanceKey);
        }
        ApproveFlowInstance flowInstance = ApproveFlowInstanceConverter.INSTANCE.entityToModel(entity);

        if (flowInstance.getApproveStatus() == ApproveStatus.REMOVED) {
            throw ResourceException.buildStatusException(this.getClass(), "the resource has been removed", approveInstanceKey);
        }

        return flowInstance;
    }

    public PageBody<ApproveFlowInstanceBase> page(ApproveRequest approveRequest,
                                                  boolean isProposer,
                                                  boolean isApprover) {
        PageBody<ApproveFlowInstanceBase> result = new PageBody<>();
        Page<ApproveFlowInstanceEntity> queryPage = new Page<>(approveRequest.getPage(), approveRequest.getSize());
        QueryWrapper<ApproveFlowInstanceEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(StringUtils.isNotEmpty(approveRequest.getApproveType()), ApproveFlowInstanceEntity::getApproveType, approveRequest.getApproveType())
                .eq(StringUtils.isNotEmpty(approveRequest.getBelongTo()), ApproveFlowInstanceEntity::getBelongTo, approveRequest.getBelongTo())
                .eq(approveRequest.getProposerId()!=null, ApproveFlowInstanceEntity::getProposerId, approveRequest.getProposerId())
                .eq(approveRequest.getStatus() != null, ApproveFlowInstanceEntity::getApproveStatus, approveRequest.getStatus())
                .ne(ApproveFlowInstanceEntity::getApproveStatus, ApproveStatus.REMOVED)
                .between(approveRequest.getStartDate() != null && approveRequest.getEndDate() != null, ApproveFlowInstanceEntity::getStartTime, approveRequest.getStartDate(), approveRequest.getEndDate());
        if (isProposer) {
            qw.lambda().eq(ApproveFlowInstanceEntity::getProposerId, CCH.userId());
        } else if (isApprover) {
            qw.lambda().eq(ApproveFlowInstanceEntity::getApproverId, CCH.userId());
            qw.lambda().eq(approveRequest.getProposerId()!=null, ApproveFlowInstanceEntity::getProposerId, approveRequest.getProposerId());
        }

        qw.orderByDesc(StringUtils.isNotEmpty(approveRequest.getOrderBy()), approveRequest.getOrderBy());
        qw.lambda().orderByDesc(ApproveFlowInstanceEntity::getUpdatedTime);
        Page<ApproveFlowInstanceEntity> page = this.page(queryPage, qw);
        result.setCurrent(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(CollectionUtils.isEmpty(page.getRecords()) ? new ArrayList<>() :
                ApproveFlowInstanceConverter.INSTANCE.entitiesToBaseModel(page.getRecords()));


        return result;
    }

    public ApproveFlowInstance getByFlowExecutionId(String flowExecutionId) {
        QueryWrapper<ApproveFlowInstanceEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ApproveFlowInstanceEntity::getFlowExecutionId, flowExecutionId);
        return ApproveFlowInstanceConverter.INSTANCE.entityToModel(this.getOne(qw));
    }

    public ApproveStatus getApproveStatusByFlowExecutionId(String flowExecutionId) {
        QueryWrapper<ApproveFlowInstanceEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ApproveFlowInstanceEntity::getFlowExecutionId, flowExecutionId);
        ApproveFlowInstanceEntity entity = this.getOne(qw);
        if (entity == null) {
            return null;
        }
        return ApproveStatus.valueOf(entity.getApproveStatus());
    }

    public boolean bindFlowExecutionId(String approveInstanceKey, String flowExecutionId) {
        UpdateWrapper<ApproveFlowInstanceEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ApproveFlowInstanceEntity::getFlowExecutionId, flowExecutionId)
                .eq(ApproveFlowInstanceEntity::getApproveInstanceKey, approveInstanceKey);
        return this.update(uw);
    }

    public boolean updateCurrentNodeKey(String approveInstanceKey, String currentNodeKey){
        UpdateWrapper<ApproveFlowInstanceEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ApproveFlowInstanceEntity::getCurrentNodeKey, currentNodeKey)
                .eq(ApproveFlowInstanceEntity::getApproveInstanceKey, approveInstanceKey);
        return this.update(uw);
    }

    public boolean save(ApproveFlowInstance approveFlowInstance){
        ApproveFlowInstanceEntity entity = ApproveFlowInstanceConverter.INSTANCE.modelToEntity(approveFlowInstance);
        return super.saveOrUpdate(entity);
    }


    private boolean updateApproveStatus(String approveInstanceKey, ApproveStatus approveStatus, String message) {
        UpdateWrapper<ApproveFlowInstanceEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(ApproveFlowInstanceEntity::getApproveStatus, approveStatus.name())
                .set(approveStatus == ApproveStatus.REJECTED ||
                        approveStatus == ApproveStatus.APPROVED, ApproveFlowInstanceEntity::getLastApproveDateTime, LocalDateTime.now())
                .set(approveStatus != ApproveStatus.STARTING &&
                                approveStatus != ApproveStatus.PROCESSING,
                        ApproveFlowInstanceEntity::getEndTime, LocalDateTime.now())
                .set(message != null, ApproveFlowInstanceEntity::getMessage, message)
                .eq(ApproveFlowInstanceEntity::getApproveInstanceKey, approveInstanceKey);
        return this.update(uw);
    }

}
