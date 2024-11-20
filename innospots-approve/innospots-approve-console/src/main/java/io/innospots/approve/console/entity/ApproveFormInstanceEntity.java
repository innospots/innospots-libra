package io.innospots.approve.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.innospots.base.entity.PBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/19
 */
@Setter
@Getter
@MappedSuperclass
public class ApproveFormInstanceEntity extends PBaseEntity {

    @Id
    @TableId(type = IdType.INPUT)
    @Column(length = 16)
    protected String appInstanceKey;

    @Column(length = 32)
    protected String flowKey;

    @Column(length = 16)
    protected String appKey;

    @Column(length = 32)
    protected String belongTo;

    @Column(length = 32)
    protected String approveType;

    @Column(length = 32)
    protected String approveStatus;

    @Column
    protected Integer originatorId;

    @Column(length = 128)
    protected String originator;

    @Column(length = 2048)
    protected String message;


}
