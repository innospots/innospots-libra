package io.innospots.app.core.model;

import io.innospots.app.core.enums.AppType;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.PBaseModelInfo;
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
@Schema(title = "only include app base info")
public class BaseAppInfo extends PBaseModelInfo {

    @Schema(title = "name")
    protected String name;

    @Schema(title = "category Id")
    protected Integer categoryId;

    @Schema(title = "description")
    protected String description;

    @Schema(title = "status")
    protected DataStatus status;

    @Schema(title = "icon")
    protected String icon;

    @Schema(title = "app page, apis resources")
    protected AppResource resources;

    @Schema(title = "app setting")
    protected AppSetting settings;

    @Schema(title = "app vendor")
    protected String vendor;

    @Schema(title = "app type")
    protected AppType appType;

}
