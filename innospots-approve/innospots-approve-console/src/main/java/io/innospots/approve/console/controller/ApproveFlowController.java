package io.innospots.approve.console.controller;

import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.runtime.ApproveFlowRuntimeContainer;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "approve/flow")
@ModuleMenu(menuKey = "approve")
@Tag(name = "approve flow")
public class ApproveFlowController {


    private final ApproveFlowInstanceOperator instanceOperator;

    private final ApproveFlowRuntimeContainer approveFlowRuntimeContainer;

    public ApproveFlowController(ApproveFlowInstanceOperator instanceOperator,
                                 ApproveFlowRuntimeContainer approveFlowRuntimeContainer) {
        this.instanceOperator = instanceOperator;
        this.approveFlowRuntimeContainer = approveFlowRuntimeContainer;
    }

    @Operation(summary = "start approve flow")
    @PostMapping("start/{approveInstanceKey}")
    public R<ApproveFlowInstance> start(@PathVariable String approveInstanceKey){

        return R.success(instanceOperator.start(approveInstanceKey));
    }

    @Operation(summary = "revoke approve flow")
    @PostMapping("revoke/{approveInstanceKey}")
    public R<Boolean> revoke(@PathVariable String approveInstanceKey, String message){
        return R.success(instanceOperator.revoke(approveInstanceKey, message));
    }

    @Operation(summary = "approve flow")
    @PostMapping("approved/{approveInstanceKey}")
    public R<Boolean> approved(@PathVariable String approveInstanceKey, String message){
        return R.success(instanceOperator.approve(approveInstanceKey, message));
    }

    @Operation(summary = "reject approve flow")
    @PostMapping("reject/{approveInstanceKey}")
    public R<Boolean> reject(@PathVariable String approveInstanceKey, String message){
        return R.success(instanceOperator.reject(approveInstanceKey, message));
    }

}
