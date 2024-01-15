package io.innospots.app.core.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.innospots.base.enums.DataStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Getter
@Setter
@Schema(title = "only include base info")
public class BaseAppInfo {

    @Schema(title = "app primary key")
    protected String appKey;

    @Schema(title = "template primary key")
    protected String templateKey;

    protected String name;

    protected Integer categoryId;

    protected String description;

    protected DataStatus status;

    protected String icon;

    protected LocalDateTime updatedTime;

    protected String createdBy;

    protected LocalDate publishTime;

    protected String author;

}
