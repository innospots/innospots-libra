package io.innospots.app.console.controller;

import io.innospots.app.core.model.AppTemplate;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "app/template")
@ModuleMenu(menuKey = "app-template")
@Tag(name = "application template")
public class AppTemplateController extends BaseController {

    @GetMapping("{templateKey}")
    @Operation(summary = "get application")
    public R<AppTemplate> getAppDefinition(
            @Parameter(required = true, name = "templateKey") @PathVariable String templateKey) {

        return R.success();
    }
}
