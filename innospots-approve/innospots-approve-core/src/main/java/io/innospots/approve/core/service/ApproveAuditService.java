package io.innospots.approve.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.innospots.approve.core.converter.ApproveFlowInstanceConverter;
import io.innospots.approve.core.dao.ApproveFlowInstanceDao;
import io.innospots.approve.core.entity.ApproveActorEntity;
import io.innospots.approve.core.entity.ApproveFlowInstanceEntity;
import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.model.ApproveActorFlowInstance;
import io.innospots.approve.core.model.ApproveFlowInstanceBase;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.user.UserInfo;
import io.innospots.base.utils.CCH;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/5
 */
@Service
public class ApproveAuditService {

    private final ApproveFlowInstanceDao approveFlowInstanceDao;

    public ApproveAuditService(ApproveFlowInstanceDao approveFlowInstanceDao) {
        this.approveFlowInstanceDao = approveFlowInstanceDao;
    }

    public PageBody<ApproveFlowInstanceBase> pageAudit(Integer page, Integer size,
                                                       String approveType,
                                                       LocalDateTime startTime, LocalDateTime endTime){
        //1. if actorType == user then actorId = userId
        //2. if actorType == role then actorId = roleId
        //3. if actorType == group then actorId = groupId
        UserInfo userInfo = ApproveHolder.getLoginUser();
        Page<ApproveFlowInstanceEntity> pg = new Page<>(page,size);
        IPage<ApproveFlowInstanceEntity> pgEntities = approveFlowInstanceDao.selectPendingApproveFlowInstances(pg,
                userInfo.getRoleIds(),userInfo.getUserId(),userInfo.getGroupId(),
                approveType,startTime,endTime);
        PageBody<ApproveFlowInstanceBase> pageBody = new PageBody<>();
        List<ApproveFlowInstanceBase> list = ApproveFlowInstanceConverter.
                INSTANCE.entitiesToBaseModel(pgEntities.getRecords());
        pageBody.setList(list);
        pageBody.setTotal(pgEntities.getTotal());
        pageBody.setPageSize(pgEntities.getSize());
        pageBody.setCurrent(pgEntities.getCurrent());
        pageBody.setTotalPage(pgEntities.getPages());

        return pageBody;
    }

    public PageBody<ApproveActorFlowInstance> pageAuditHistory(Integer page, Integer size,
                                                              String approveType,
                                                              LocalDateTime startTime, LocalDateTime endTime){

        Page<ApproveActorFlowInstance> pg = new Page<>(page,size);
        if("".equals(approveType)){
            approveType = null;
        }
        IPage<ApproveActorFlowInstance> pgEntities = approveFlowInstanceDao
                .selectApproveAuditHistoryFlowInstances(pg,CCH.userId(),approveType,startTime,endTime);
        PageBody<ApproveActorFlowInstance> pageBody = new PageBody<>();
        List<ApproveActorFlowInstance> list = pgEntities.getRecords();
        pageBody.setList(list);
        pageBody.setTotal(pgEntities.getTotal());
        pageBody.setPageSize(pgEntities.getSize());
        pageBody.setCurrent(pgEntities.getCurrent());
        pageBody.setTotalPage(pg.getPages());

        return pageBody;
    }

}
