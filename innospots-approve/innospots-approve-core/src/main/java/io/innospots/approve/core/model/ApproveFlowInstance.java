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
public class ApproveFlowInstance extends ApproveFlowInstanceBase {

    private String flowExecutionId;

    private Map<String, Object> formData;

    private String currentNodeKey;


    @Override
    public String toString() {
        return "{" +
                "flowExecutionId='" + flowExecutionId + '\'' +
                ", currentNodeKey='" + currentNodeKey + '\'' +
                ", approveInstanceKey='" + approveInstanceKey + '\'' +
                ", flowKey='" + flowKey + '\'' +
                ", appKey='" + appKey + '\'' +
                ", belongTo='" + belongTo + '\'' +
                ", proposer='" + proposer + '\'' +
                ", proposerId=" + proposerId +
                ", message='" + message + '\'' +
                ", approver='" + approver + '\'' +
                ", approverId=" + approverId +
                ", approveStatus=" + approveStatus +
                ", result='" + result + '\'' +
                ", approveType='" + approveType + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", lastApproveDateTime=" + lastApproveDateTime +
                '}';
    }
}
