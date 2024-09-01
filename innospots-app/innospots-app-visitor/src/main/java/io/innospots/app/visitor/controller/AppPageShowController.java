package io.innospots.app.visitor.controller;

import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.visitor.model.AppToken;
import io.innospots.app.visitor.model.RequestAccess;
import io.innospots.app.visitor.operator.AppShowVisitor;
import io.innospots.base.constant.PathConstant;
import io.innospots.base.model.response.InnospotsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;


/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/26
 */
@RestController
@RequestMapping( PathConstant.APP_ROOT_PATH +"api/page")
@Tag(name = "Application Page Show")
public class AppPageShowController {

    private final AppShowVisitor appShowVisitor;

    public AppPageShowController(AppShowVisitor appShowVisitor) {
        this.appShowVisitor = appShowVisitor;
    }

    @Operation(summary = "check access according appKey")
    @PostMapping("{appPath}/access/check")
    public InnospotsResponse<AppToken> valid(@PathVariable String appPath,
                                             @RequestBody RequestAccess requestAccess){
        return appShowVisitor.checkAccess(appPath,requestAccess);
    }

    @Operation(summary = "get app definition page")
    @GetMapping("{appPath}/show")
    public InnospotsResponse<AppDefinition> showPage(@PathVariable String appPath){
        return InnospotsResponse.success(appShowVisitor.getAppDefinitionByAppPath(appPath));
    }

}
