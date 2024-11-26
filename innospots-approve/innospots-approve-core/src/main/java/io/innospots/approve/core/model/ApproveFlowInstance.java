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

    private List<String> nextNodeKeys;

}
