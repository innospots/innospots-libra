package io.innospots.approve.core.model;

import io.innospots.approve.core.enums.ActorType;
import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.base.json.annotation.I18n;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
@Getter
@Setter
public class ApproveActorFlowInstance extends ApproveFlowInstanceBase {

    private String userName;
    private ActorType actorType;
    private ApproveAction approveAction;
    private String nodeKey;
    private String actMsg;
    private String actResult;
    protected LocalDateTime optTime;

}
