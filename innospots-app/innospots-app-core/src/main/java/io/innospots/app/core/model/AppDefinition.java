package io.innospots.app.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Setter
@Getter
public class AppDefinition extends BaseAppInfo {

    @Schema(title = "app definition primary key")
    private String appKey;

    @Schema(title = "app definition template key")
    private String templateKey;

    /**
     * 应用路径
     */
    @Schema(title = "app url path, when share to public")
    private String appPath;
}
