package io.innospots.app.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/10
 */
@Setter
@Getter
@Schema(title = "app setting")
public class AppSetting {

    private Boolean showNavigation;

    private NaviLocation naviLocation;

    private Boolean publicVisit;

    private Boolean embedShowNavigation;

    public enum NaviLocation {
        TOP,
        LEFT;
    }
}
