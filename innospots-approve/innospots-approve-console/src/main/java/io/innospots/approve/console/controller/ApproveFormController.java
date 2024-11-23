package io.innospots.approve.console.controller;

import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveForm;
import io.innospots.approve.core.model.ApproveRequest;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "approve")
@ModuleMenu(menuKey = "approve")
@Tag(name = "approve form")
public class ApproveFormController {

    private final ApproveFlowInstanceOperator approveFlowInstanceOperator;

    public ApproveFormController(ApproveFlowInstanceOperator approveFlowInstanceOperator) {
        this.approveFlowInstanceOperator = approveFlowInstanceOperator;
    }

    @Operation(summary = "create or save approve form data")
    @PostMapping("save")
    public R<ApproveFlowInstance> save(ApproveForm approveForm) {
        return R.success(approveFlowInstanceOperator.save(approveForm));
    }

    @Operation(summary = "update removed status")
    @DeleteMapping("/key/{approveInstanceKey}")
    public R<ApproveFlowInstance> remove(String approveInstanceKey) {
        approveFlowInstanceOperator.remove(approveInstanceKey);
        return null;
    }

    @Operation(summary = "page approver instances by request")
    @GetMapping("page/approver")
    public PageBody<ApproveFlowInstance> pageByApprover(ApproveRequest approveRequest) {
        return approveFlowInstanceOperator.page(approveRequest, false, true);
    }


    @Operation(summary = "page originator instances by request")
    @GetMapping("page/originator")
    public PageBody<ApproveFlowInstance> pageByOriginator(ApproveRequest approveRequest) {
        return approveFlowInstanceOperator.page(approveRequest, true, false);
    }

    @Operation(summary = "get approve instance by approve instance key")
    @GetMapping("/key/{approveInstanceKey}")
    public ApproveFlowInstance findOne(String approveInstanceKey) {
        return approveFlowInstanceOperator.findOne(approveInstanceKey);
    }


}
