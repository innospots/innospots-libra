package io.innospots.app.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Getter
@Setter
public class AppTemplate extends BaseAppInfo {

    @Schema(title = "app setting")
    protected AppSetting settings;

    @Schema(title = "app page, apis resources")
    protected AppResource resources;

}
