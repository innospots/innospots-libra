package io.innospots.approve.core.model;

import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.enums.ApproveResult;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Getter
@Setter
@Builder
public class ApproveActor {

    private String approveActorId;

    private String approveInstanceKey;

    private String nodeKey;

    private Integer userId;

    private String userName;

    private String actorType;

    private ApproveAction approveAction;

    private String result;

    private String message;

}
