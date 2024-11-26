package io.innospots.approve.console.controller;

import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveRequest;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.runtime.ApproveFlowRuntimeContainer;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "approve/audit")
@ModuleMenu(menuKey = "approve")
@Tag(name = "approve audit")
public class ApproveAuditController {


    private final ApproveFlowInstanceOperator approveFlowInstanceOperator;

    private final ApproveFlowRuntimeContainer approveFlowRuntimeContainer;

    public ApproveAuditController(ApproveFlowInstanceOperator approveFlowInstanceOperator,
                                  ApproveFlowRuntimeContainer approveFlowRuntimeContainer) {
        this.approveFlowInstanceOperator = approveFlowInstanceOperator;
        this.approveFlowRuntimeContainer = approveFlowRuntimeContainer;
    }


    @Operation(summary = "page approver instances by request")
    @GetMapping("page")
    public PageBody<ApproveFlowInstance> pageByApprover(ApproveRequest approveRequest) {
        return approveFlowInstanceOperator.page(approveRequest, false, true);
    }

    @Operation(summary = "get approve instance by approve instance key")
    @GetMapping("/key/{approveInstanceKey}")
    public ApproveFlowInstance findOne(String approveInstanceKey) {
        return approveFlowInstanceOperator.findOne(approveInstanceKey);
    }

    @Operation(summary = "approve flow")
    @PostMapping("approved/{approveInstanceKey}")
    public R<Boolean> approved(@PathVariable String approveInstanceKey, String message){
        return R.success(approveFlowRuntimeContainer.approve(approveInstanceKey, message));
    }

    @Operation(summary = "reject approve flow")
    @PostMapping("reject/{approveInstanceKey}")
    public R<Boolean> reject(@PathVariable String approveInstanceKey, String message){
        return R.success(approveFlowRuntimeContainer.reject(approveInstanceKey, message));
    }

}
