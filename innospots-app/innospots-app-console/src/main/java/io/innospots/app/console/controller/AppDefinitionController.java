package io.innospots.app.console.controller;

import io.innospots.app.core.model.form.AppQueryRequest;
import io.innospots.app.core.model.form.CreateAppFrom;
import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.response.R;
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
    @Operation(description = "get application")
    public R<AppDefinition> getAppDefinition(
            @Parameter(required = true, name = "appKey") @PathVariable String appKey) {
        AppDefinition appDefinition = appDefinitionOperator.getAppDefinition(appKey);
        return R.success(appDefinition);
    }

    @PostMapping
    @Operation(description = "create application")
    public R<AppDefinition> createAppDefinition(
            @Parameter(required = true, name = "createAppFrom") @RequestBody CreateAppFrom createAppFrom) {
        AppDefinition appDefinition = appDefinitionOperator.createAppDefinition(createAppFrom);
        return R.success(appDefinition);
    }

    @PutMapping
    @Operation(description = "update application")
    public R<AppDefinition> updateAppDefinition(@RequestBody AppDefinition appDefinition) {
        AppDefinition updateAppDefinition = appDefinitionOperator.updateAppDefinition(appDefinition);
        return R.success(updateAppDefinition);
    }

    @PutMapping("{appKey}/status/{status}")
    @Operation(description = "update application status")
    public R<Boolean> updateStatus(@PathVariable String appKey, @PathVariable DataStatus status) {
        return R.success(appDefinitionOperator.updateStatus(appKey, status));
    }

    @DeleteMapping("{appKey}")
    @Operation(description = "delete application")
    public R<Boolean> deleteAppDefinition(@PathVariable String appKey) {
        return R.success(appDefinitionOperator.deleteAppDefinition(appKey));
    }

    @GetMapping("page")
    @Operation(description = "application page")
    public R<PageBody<AppDefinition>> pageAppInfos(AppQueryRequest queryRequest) {
        return R.success(appDefinitionOperator.pageAppInfos(queryRequest));
    }

}
