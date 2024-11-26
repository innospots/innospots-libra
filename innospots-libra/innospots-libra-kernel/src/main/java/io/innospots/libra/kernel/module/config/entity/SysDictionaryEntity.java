package io.innospots.libra.kernel.module.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.base.json.annotation.I18n;
import io.innospots.libra.kernel.module.system.entity.SysRoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/26
 */
@Getter
@Setter
@Entity
@TableName(value = SysDictionaryEntity.TABLE_NAME)
@Table(name = SysDictionaryEntity.TABLE_NAME)
public class SysDictionaryEntity extends PBaseEntity {

    public static final String TABLE_NAME = "sys_dictionary";

    @Id
    @TableId(type = IdType.ASSIGN_ID)
    @Column(length = 32)
    private String dictId;

    @Column(length = 256)
    private String name;

    @Column(length = 64)
    private String value;

    @Column(length = 32)
    private String type;

    @Column(length = 256)
    private String typeName;

    @Column
    private Boolean status;

}
