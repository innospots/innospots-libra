package io.innospots.app.console.controller;

import io.innospots.app.core.model.form.AppQueryRequest;
import io.innospots.app.core.model.form.CreateAppFrom;
import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "app/definition")
@ModuleMenu(menuKey = "applications")
@Tag(name = "application definition")
public class AppDefinitionController extends BaseController {

    private final AppDefinitionOperator appDefinitionOperator;

    public AppDefinitionController(AppDefinitionOperator appDefinitionOperator) {
        this.appDefinitionOperator = appDefinitionOperator;
    }

    @GetMapping("{appKey}")
    @Operation(summary = "get application")
    public InnospotResponse<AppDefinition> getAppDefinition(
            @Parameter(required = true, name = "appKey") @PathVariable String appKey) {
        AppDefinition appDefinition = appDefinitionOperator.getAppDefinition(appKey);
        return InnospotResponse.success(appDefinition);
    }

    @PostMapping
    @Operation(summary = "create application")
    public InnospotResponse<AppDefinition> createAppDefinition(
            @Parameter(required = true, name = "createAppFrom") @RequestBody CreateAppFrom createAppFrom) {
        AppDefinition appDefinition = appDefinitionOperator.createAppDefinition(createAppFrom);
        return InnospotResponse.success(appDefinition);
    }

    @PutMapping
    @Operation(summary = "update application")
    public InnospotResponse<AppDefinition> updateAppDefinition(@RequestBody AppDefinition appDefinition) {
        AppDefinition updateAppDefinition = appDefinitionOperator.updateAppDefinition(appDefinition);
        return InnospotResponse.success(updateAppDefinition);
    }

    @PutMapping("{appKey}/status/{status}")
    @Operation(summary = "update application status")
    public InnospotResponse<Boolean> updateStatus(@PathVariable String appKey, @PathVariable DataStatus status) {
        return InnospotResponse.success(appDefinitionOperator.updateStatus(appKey, status));
    }

    @DeleteMapping("{appKey}")
    @Operation(summary = "delete application")
    public InnospotResponse<Boolean> deleteAppDefinition(@PathVariable String appKey) {
        return InnospotResponse.success(appDefinitionOperator.deleteAppDefinition(appKey));
    }

    @GetMapping("page")
    @Operation(summary = "application page")
    public InnospotResponse<PageBody<AppDefinition>> pageAppInfos(AppQueryRequest queryRequest) {
        return InnospotResponse.success(appDefinitionOperator.pageAppInfos(queryRequest));
    }

}
