package io.innospots.approve.console.controller;

import io.innospots.approve.core.model.ApproveActorFlowInstance;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.model.ApproveFlowInstanceBase;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.runtime.ApproveFlowRuntimeContainer;
import io.innospots.approve.core.service.ApproveAuditService;
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
@RequestMapping(PATH_ROOT_ADMIN + "approve/audit")
@ModuleMenu(menuKey = "approve")
@Tag(name = "approve audit")
public class ApproveAuditController {


    private final ApproveFlowInstanceOperator approveFlowInstanceOperator;

    private final ApproveFlowRuntimeContainer approveFlowRuntimeContainer;

    private final ApproveAuditService approveAuditService;

    public ApproveAuditController(ApproveFlowInstanceOperator approveFlowInstanceOperator,
                                  ApproveFlowRuntimeContainer approveFlowRuntimeContainer, ApproveAuditService approveAuditService) {
        this.approveFlowInstanceOperator = approveFlowInstanceOperator;
        this.approveFlowRuntimeContainer = approveFlowRuntimeContainer;
        this.approveAuditService = approveAuditService;
    }


    @Operation(summary = "page approver instances by request")
    @GetMapping("page")
    public R<PageBody<ApproveFlowInstanceBase>> pageByApprover(
            @RequestParam(required = false) String approveType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return R.success(approveAuditService.pageAudit(page,size,approveType,startTime,endTime));
    }

    @Operation(summary = "page history approver instances by request")
    @GetMapping("page/history")
    public R<PageBody<ApproveActorFlowInstance>> pageHistoryByApprover(
            @RequestParam(required = false) String approveType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return R.success(approveAuditService.pageAuditHistory(page,size,approveType,startTime,endTime));
    }

    @Operation(summary = "get approve instance by approve instance key")
    @GetMapping("/key/{approveInstanceKey}")
    public R<ApproveFlowInstance> findOne(@PathVariable String approveInstanceKey) {
        return R.success(approveFlowInstanceOperator.findOne(approveInstanceKey));
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
