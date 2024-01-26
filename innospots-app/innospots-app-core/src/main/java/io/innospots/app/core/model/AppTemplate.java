package io.innospots.app.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Getter
@Setter
@Schema(title = "app template")
public class AppTemplate extends BaseAppInfo {

    @Schema(title = "template primary key")
    private String templateKey;

    @Schema(title = "publish time")
    private LocalDate publishTime;

    @Schema(title = "author")
    private String author;
}
