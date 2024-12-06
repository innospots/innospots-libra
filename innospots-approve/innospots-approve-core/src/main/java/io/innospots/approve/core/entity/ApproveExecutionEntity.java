package io.innospots.approve.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/19
 */
@Getter
@Setter
@Entity
@Table(name = ApproveExecutionEntity.TABLE_NAME,indexes = {@Index(name = "idx_flow_execution", columnList = "flowExecutionId")})
@TableName(value = ApproveExecutionEntity.TABLE_NAME)
public class ApproveExecutionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "approve_flow_execution";

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 64)
    private String approveExecutionId;

    @Column(length = 32)
    private String approveInstanceKey;

    @Column(length = 64)
    private String flowExecutionId;

    @Column(length = 32)
    private String approveActorId;

    @Column(length = 64)
    private String nodeKey;

    @Column(length = 32)
    private String nodeName;

    @Column
    private Integer userId;

    @Column(length = 64)
    private String userName;

    @Column
    private Integer sequenceNumber;

    @Column(length = 32)
    private String result;

    @Column(length = 32)
    private String approveResult;

    @Column(length = 32)
    private String executionStatus;

    @Column(length = 2048)
    private String message;


    @Column(columnDefinition = "TEXT")
    private String context;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

}
