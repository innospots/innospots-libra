package io.innospots.approve.console.controller;

import io.innospots.approve.core.model.ApproveExecution;
import io.innospots.approve.core.service.ApproveExecutionService;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/6
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "approve/execution")
@ModuleMenu(menuKey = "approve")
@Tag(name = "approve execution")
public class ApproveExecutionController {

    private final ApproveExecutionService approveExecutionService;

    public ApproveExecutionController(ApproveExecutionService approveExecutionService) {
        this.approveExecutionService = approveExecutionService;
    }

    @GetMapping("{approveInstanceKey}")
    public R<List<ApproveExecution>> currentPage(@PathVariable String approveInstanceKey){
        return R.success(approveExecutionService.listCurrentApproveExecutions(approveInstanceKey));
    }

}
