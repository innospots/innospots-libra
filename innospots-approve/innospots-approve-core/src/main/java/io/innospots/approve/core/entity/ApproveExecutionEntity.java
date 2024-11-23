package io.innospots.approve.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.base.entity.PBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/19
 */
@Getter
@Setter
@Entity
@Table(name = ApproveExecutionEntity.TABLE_NAME)
@TableName(value = ApproveExecutionEntity.TABLE_NAME)
public class ApproveExecutionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "approve_flow_execution";

    @Id
    @TableId(type = IdType.ASSIGN_ID)
    @Column(length = 16)
    private String approveExecutionId;

    @Column(length = 16)
    private String approveInstanceKey;


    @Column(length = 128)
    private String approverRole;

    @Column
    private Integer approverRoleId;


    @Column(length = 64)
    private String currentNodeKey;

    @Column(length = 32)
    private String approveStatus;


    @Column(length = 2048)
    private String message;


    @Column(columnDefinition = "TEXT")
    private String context;

}
