package io.innospots.app.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.base.enums.DataStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/7
 */
@Getter
@Setter
@Entity
@Table(name = AppTemplateEntity.TABLE_NAME)
@TableName(value = AppTemplateEntity.TABLE_NAME)
public class AppTemplateEntity extends BaseAppEntity {

    public static final String TABLE_NAME = "app_template";

    @Id
    @TableId(type = IdType.INPUT)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(length = 16)
    private String templateKey;

    @Column
    private LocalDate publishTime;

    @Column(length = 64)
    private String author;

}
