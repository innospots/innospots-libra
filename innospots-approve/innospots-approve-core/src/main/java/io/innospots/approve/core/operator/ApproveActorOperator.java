package io.innospots.approve.core.operator;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        return null;
    }

    public ApproveActor saveApproveActor(ApproveActor approveActor) {

        return approveActor;
    }

}
