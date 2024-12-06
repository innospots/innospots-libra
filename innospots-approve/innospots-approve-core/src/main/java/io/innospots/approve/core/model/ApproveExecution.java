package io.innospots.approve.core.model;

import io.innospots.approve.core.enums.ApproveAction;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.base.quartz.ExecutionStatus;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Getter
@Setter
public class ApproveExecution implements Comparable<ApproveExecution>{

    /**
     * nodeExecutionId
     */
    private String approveExecutionId;

    private String flowExecutionId;

    private String approveInstanceKey;

    private String nodeKey;

    private String nodeName;

    private Integer userId;

    private String userName;

    private Integer sequenceNumber;

    private String result;

    private ExecutionStatus executionStatus;

    private ApproveStatus approveStatus;

    private String message;

    private String context;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public Map<String, Object> toInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("approveExecutionId", approveExecutionId);
        info.put("userId", userId);
        info.put("userName", userName);
        info.put("result", result);
        info.put("approveStatus", approveStatus.name());
        info.put("message", message);
        info.put("startTime", startTime);
        info.put("endTime", endTime);
        return info;
    }

    @Override
    public int compareTo(@NotNull ApproveExecution o) {
        return o.endTime.compareTo(endTime);
    }
}
