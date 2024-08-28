package io.innospots.app.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
public class AppDefinitionEntity extends BaseAppEntity {

    public static final String TABLE_NAME = "app_definition";

    @Id
    @TableId(type = IdType.INPUT)
//    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(length = 16)
    private String appKey;

    @Column(length = 16)
    private String templateKey;

    @Column(length = 32)
    private String appPath;

    @Column(length = 16)
    private String accessKey;

}
