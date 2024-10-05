package io.innospots.app.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Setter
@Getter
@Schema(title = "app page config, which includes the page components")
public class AppPage {

    @Schema(title = "page primary key")
    private String pageKey;

    @Schema(title = "form element components")
    private Map<String,Object> components;

    @Schema(title = "page name")
    private String pageName;

    @Schema(title = "page url")
    private String pageUrl;

    @Schema(title = "show navigation")
    private Boolean showNavigation;

    @Schema(title = "set home page")
    private Boolean setHomePage;

    @Schema(title = "page config")
    private Map<String,Object> configs;
}
