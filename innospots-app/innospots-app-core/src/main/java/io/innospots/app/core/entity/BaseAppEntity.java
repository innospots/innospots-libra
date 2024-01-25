package io.innospots.app.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.base.enums.DataStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/7
 */
@Setter
@Getter
@MappedSuperclass
public class BaseAppEntity extends PBaseEntity {

    @Column(length = 32)
    protected String name;

    @Column
    protected Integer categoryId;

    @Column(length = 256)
    protected String description;

    @Column(length = 32)
    @Enumerated(value = EnumType.STRING)
    protected DataStatus status;

    @Column(length = 32)
    protected String icon;

    @Column(columnDefinition = "MEDIUMTEXT")
    protected String resources;

    @Column(columnDefinition = "TEXT")
    protected String settings;

}
