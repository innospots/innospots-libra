package io.innospots.approve.core.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.approve.core.converter.ApproveActorConverter;
import io.innospots.approve.core.dao.ApproveActorDao;
import io.innospots.approve.core.entity.ApproveActorEntity;
import io.innospots.approve.core.model.ApproveActor;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Component
public class ApproveActorOperator extends ServiceImpl<ApproveActorDao, ApproveActorEntity> {

    public ApproveActor getApproveActor(String approveInstanceKey,String nodeKey) {
        QueryWrapper<ApproveActorEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(ApproveActorEntity::getApproveInstanceKey, approveInstanceKey)
                .eq(ApproveActorEntity::getNodeKey, nodeKey);
        ApproveActorEntity entity = this.getOne(qw);
        return ApproveActorConverter.INSTANCE.entityToModel(entity);
    }

    public ApproveActor saveApproveActor(ApproveActor approveActor) {
        ApproveActorEntity entity = ApproveActorConverter.INSTANCE.modelToEntity(approveActor);
        this.save(entity);
        return approveActor;
    }

}
