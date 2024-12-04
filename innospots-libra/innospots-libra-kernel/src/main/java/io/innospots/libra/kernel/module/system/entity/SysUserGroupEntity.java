package io.innospots.libra.kernel.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.BaseEntity;
import io.innospots.base.entity.PBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/3
 */

@Getter
@Setter
@Entity
@TableName(value = SysUserGroupEntity.TABLE_NAME)
@Table(name = SysUserGroupEntity.TABLE_NAME)
public class SysUserGroupEntity extends PBaseEntity {

    public static final String TABLE_NAME = "sys_user_group";

    @Id
    @TableId(type = IdType.AUTO)
    private Integer groupId;

    @Column(length = 16)
    private String groupCode;

    @Column(length = 128)
    private String groupName;

    @Column
    private Integer parentGroupId;

    @Column
    private Integer headUserId;

    @Column(length = 128)
    private String assistantUserIds;

}
