package io.innospots.app.console.controller;

import io.innospots.app.console.form.AppQueryRequest;
import io.innospots.app.console.form.CreateAppFrom;
import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.model.BaseAppInfo;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN+"app/definition")
@ModuleMenu(menuKey = "applications")
@Tag(name = "application definition")
public class AppDefinitionController extends BaseController {

    @GetMapping("{appKey}")
    public InnospotResponse<AppDefinition> getAppDefinition(
            @Parameter(required = true, name = "appKey") @PathVariable String appKey) {

        return InnospotResponse.success();
    }

    @PostMapping
    public InnospotResponse<BaseAppInfo> createAppDefinition(
            @Parameter(required = true, name = "createAppFrom") @RequestBody CreateAppFrom createAppFrom) {

        return InnospotResponse.success();
    }

    @PutMapping("base-info")
    public InnospotResponse<BaseAppInfo> updateAppInfo(@RequestBody BaseAppInfo baseAppInfo){
        return InnospotResponse.success();
    }

    @PutMapping
    public InnospotResponse<AppDefinition> updateAppDefinition(@RequestBody AppDefinition appDefinition){
        return InnospotResponse.success();
    }

    @PutMapping("{appKey}/status/{status}")
    public InnospotResponse<Boolean> updateStatus(@PathVariable String appKey,@PathVariable DataStatus status){

        return InnospotResponse.success();
    }

    @DeleteMapping("{appKey}")
    public InnospotResponse<Boolean> deleteAppDefinition(@PathVariable String appKey){

        return InnospotResponse.success();
    }

    @GetMapping("page")
    public InnospotResponse<PageBody<BaseAppInfo>> pageAppInfos(@RequestParam AppQueryRequest queryRequest){
        return InnospotResponse.success();
    }

}
