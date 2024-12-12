package io.innospots.approve.console.controller;

import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveFlowInstanceBase;
import io.innospots.approve.core.model.ApproveForm;
import io.innospots.approve.core.model.ApproveRequest;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.runtime.ApproveFlowRuntimeContainer;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "approve/proposer")
@ModuleMenu(menuKey = "approve")
@Tag(name = "approve proposer")
public class ApproveProposerController {

    private final ApproveFlowInstanceOperator approveFlowInstanceOperator;

    private final ApproveFlowRuntimeContainer approveFlowRuntimeContainer;


    public ApproveProposerController(ApproveFlowInstanceOperator approveFlowInstanceOperator,
                                     ApproveFlowRuntimeContainer approveFlowRuntimeContainer) {
        this.approveFlowInstanceOperator = approveFlowInstanceOperator;
        this.approveFlowRuntimeContainer = approveFlowRuntimeContainer;
    }

    @Operation(description = "create or save approve form data")
    @PostMapping("save")
    public R<ApproveFlowInstance> save(@RequestBody ApproveForm approveForm) {
        return R.success(approveFlowInstanceOperator.save(approveForm));
    }

    @Operation(description = "update removed status")
    @DeleteMapping("/key/{approveInstanceKey}")
    public R<Boolean> remove(@PathVariable String approveInstanceKey) {
        return R.success(approveFlowInstanceOperator.remove(approveInstanceKey));
    }

    @Operation(description = "page proposer instances by request")
    @GetMapping("page")
    public R<PageBody<ApproveFlowInstanceBase>> pageByProposer(
            @RequestParam(required = false) String approveType,
            @RequestParam(required = false) ApproveStatus approveStatus,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) Integer proposerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        ApproveRequest approveRequest = new ApproveRequest();
        approveRequest.setApproveType(approveType);
        approveRequest.setStatus(approveStatus);
        approveRequest.setOrderBy(orderBy);
        approveRequest.setStartDate(startDate);
        approveRequest.setEndDate(endDate);
        approveRequest.setPage(page);
        approveRequest.setSize(size);
        approveRequest.setProposerId(proposerId);
        return R.success(approveFlowInstanceOperator.page(approveRequest, true, false));
    }

    @Operation(description = "get approve instance by approve instance key")
    @GetMapping("/key/{approveInstanceKey}")
    public R<ApproveFlowInstance> findOne(@PathVariable String approveInstanceKey) {
        return R.success(approveFlowInstanceOperator.findOne(approveInstanceKey));
    }

    @Operation(description = "start approve flow")
    @PostMapping("start/{approveInstanceKey}")
    public R<ApproveFlowInstance> start(@PathVariable String approveInstanceKey){
        return R.success(approveFlowRuntimeContainer.start(approveInstanceKey));
    }

    @Operation(description = "revoke approve flow")
    @PostMapping("revoke/{approveInstanceKey}")
    public R<Boolean> revoke(@PathVariable String approveInstanceKey, String message){
        return R.success(approveFlowRuntimeContainer.revoke(approveInstanceKey, message));
    }


}
