package io.innospots.app.visitor.model;

import io.innospots.app.core.model.AppResource;
import io.innospots.app.core.model.AppSetting;
import io.innospots.app.core.model.BaseAppInfo;
import io.innospots.base.enums.DataStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/28
 */
@Schema(title = "app show info")
@Getter
@Setter
public class AppShowInfo extends BaseAppInfo {


    @Schema(title = "app definition primary key")
    private String appKey;

    /**
     * app uri path
     */
    @Schema(title = "app url path, when share to public")
    private String appPath;


}
