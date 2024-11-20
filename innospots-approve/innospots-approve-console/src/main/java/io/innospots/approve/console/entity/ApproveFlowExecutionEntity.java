package io.innospots.approve.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = ApproveFlowExecutionEntity.TABLE_NAME)
@TableName(value = ApproveFlowExecutionEntity.TABLE_NAME)
public class ApproveFlowExecutionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "approve_flow_execution";

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 16)
    private String flowExecutionId;

    @Column(length = 32)
    private String approveType;


    private String appInstanceKey;

    @Column(length = 32)
    private String approveStatus;


    @Column(length = 128)
    private String originator;

    @Column
    private Integer originatorId;


    @Column(length = 128)
    private String approverRole;

    @Column
    private Integer approverRoleId;


    @Column(length = 64)
    private String nextNodeKey;

    @Column
    private LocalDateTime lastApproveDateTime;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(length = 2048)
    private String message;

}
