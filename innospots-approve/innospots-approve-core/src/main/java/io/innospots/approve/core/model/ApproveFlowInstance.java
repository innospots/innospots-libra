package io.innospots.approve.core.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.innospots.approve.core.enums.ApproveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
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
public class ApproveFlowInstance {

    private String approveInstanceKey;

    private String flowKey;

    private String appKey;

    private String flowExecutionId;

    private String belongTo;

    private String approveType;

    private Integer proposerId;

    private String proposer;

    private String message;

    private String approverRole;

    private Integer approverRoleId;

    private LocalDateTime lastApproveDateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Map<String,Object> formData;

    private ApproveStatus approveStatus;

    private List<String> nextNodeKeys;

    private LocalDateTime updatedTime;
}
