package io.innospots.approve.core.entity;

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

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
@Getter
@Setter
@Entity
@Table(name = ApproveActorEntity.TABLE_NAME)
@TableName(value = ApproveActorEntity.TABLE_NAME)
public class ApproveActorEntity extends PBaseEntity {

    public static final String TABLE_NAME = "approve_actor";

    @Id
    @TableId(type = IdType.ASSIGN_ID)
    @Column(length = 16)
    private String approveActorId;

    @Column(length = 32)
    private String approveInstanceKey;

    @Column(length = 64)
    private String nodeKey;

    @Column
    private Integer userId;

    @Column(length = 64)
    private String userName;

    @Column(length = 32)
    private String actorType;

    @Column(length = 32)
    private String approveAction;

    @Column(length = 256)
    private String result;

    @Column(length = 256)
    private String message;

}
