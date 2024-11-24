package io.innospots.approve.core.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Getter
@Setter
public class ApproveExecution {

    private String approveExecutionId;

    private String approveInstanceKey;

    private String nodeKey;

    private String nodeName;

    private Integer userId;

    private String userName;

    private Integer sequenceNumber;

    private String result;

    private String message;

    private String context;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
