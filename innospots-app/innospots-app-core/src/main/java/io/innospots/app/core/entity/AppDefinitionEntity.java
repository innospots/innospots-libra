package io.innospots.app.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@Getter
@Setter
@Entity
@TableName(value = AppDefinitionEntity.TABLE_NAME)
@Table(name = AppDefinitionEntity.TABLE_NAME)
public class AppDefinitionEntity extends PBaseEntity {

    public static final String TABLE_NAME = "app_definition";

    @Id
    @TableId(type = IdType.INPUT)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(length = 16)
    private String appKey;

    @Column(length = 32)
    private String name;

    private Integer categoryId;

    @Column(length = 256)
    private String description;

    @Column(length = 32)
    @Enumerated(value = EnumType.STRING)
    private DataStatus status;

    @Column(length = 32)
    private String icon;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String config;


    @Column(columnDefinition = "MEDIUMTEXT")
    private String executionPreview;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String executeCode;


}
