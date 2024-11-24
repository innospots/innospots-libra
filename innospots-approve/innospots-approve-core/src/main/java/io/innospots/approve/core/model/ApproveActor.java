package io.innospots.approve.core.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Getter
@Setter
public class ApproveActor {

    private String approveActorId;

    private String approveInstanceKey;

    private String nodeKey;

    private Integer userId;

    private String userName;

    private String actorType;

    private String approveAction;

    private String result;

    private String message;
}
