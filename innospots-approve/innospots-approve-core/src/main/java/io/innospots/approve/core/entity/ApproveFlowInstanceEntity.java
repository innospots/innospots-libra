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
 * @date 2024/11/19
 */
@Setter
@Getter
@Entity
@Table(name = ApproveFlowInstanceEntity.TABLE_NAME,indexes = {@Index(name = "idx_flow_execution", columnList = "flowExecutionId")})
@TableName(value = ApproveFlowInstanceEntity.TABLE_NAME)
public class ApproveFlowInstanceEntity extends PBaseEntity {

    public static final String TABLE_NAME = "approve_flow_instance";

    @Id
    @TableId(type = IdType.ASSIGN_ID)
    @Column(length = 32)
    private String approveInstanceKey;

    @Column(length = 32)
    private String flowKey;

    @Column(length = 16)
    private String appKey;

    @Column(length = 64)
    private String flowExecutionId;

    @Column(length = 32)
    private String belongTo;

    @Column(length = 32)
    private String approveType;

    @Column(length = 32)
    private String approveStatus;

    @Column
    private Integer proposerId;

    @Column(length = 128)
    private String proposer;

    @Column(length = 512)
    private String message;

    @Column(length = 128)
    private String approver;

    @Column
    private Integer approverId;

    @Column(length = 32)
    private String approveNodeKey;

    @Column(length = 32)
    private String approveNodeName;

    @Column(length = 64)
    private String currentNodeKey;

    @Column
    private LocalDateTime lastApproveDateTime;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String formData;

    @Column(length = 256)
    private String result;

}
