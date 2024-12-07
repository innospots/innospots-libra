package io.innospots.approve.core.model;

import io.innospots.approve.core.enums.ApproveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
@Setter
@Getter
public class ApproveRequest {

    private Integer page=1;
    private Integer size=20;
    private ApproveStatus status;
    private String belongTo;
    private String approveType;
    private Integer proposerId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String orderBy;
}
