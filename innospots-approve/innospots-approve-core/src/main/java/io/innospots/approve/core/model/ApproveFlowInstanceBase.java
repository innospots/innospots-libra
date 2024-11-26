package io.innospots.approve.core.model;

import io.innospots.approve.core.enums.ApproveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/19
 */
@Getter
@Setter
public class ApproveFlowInstanceBase {

    protected String approveInstanceKey;

    protected String flowKey;

    protected String appKey;

    protected String belongTo;

    protected String approveType;

    protected Integer proposerId;

    protected String proposer;

    protected String message;

    protected String approver;

    protected Integer approverId;

    protected LocalDateTime lastApproveDateTime;

    protected LocalDateTime startTime;

    protected LocalDateTime endTime;

    protected ApproveStatus approveStatus;

    protected LocalDateTime updatedTime;
}
