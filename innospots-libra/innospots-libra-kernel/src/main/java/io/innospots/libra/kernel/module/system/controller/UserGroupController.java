package io.innospots.libra.kernel.module.system.controller;

import io.innospots.base.model.response.R;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.base.model.user.UserSimpleGroup;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.innospots.libra.kernel.module.system.model.user.GroupForm;
import io.innospots.libra.kernel.module.system.model.user.UserGroup;
import io.innospots.libra.kernel.module.system.operator.UserGroupOperator;
import io.innospots.libra.kernel.module.system.service.UserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/7
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "user-group")
@ModuleMenu(menuKey = "libra-user-group")
@Tag(name = "System User Group")
public class UserGroupController {

    private UserGroupService userGroupService;

    private UserGroupOperator userGroupOperator;

    public UserGroupController(UserGroupService userGroupService,
                               UserGroupOperator userGroupOperator) {
        this.userGroupService = userGroupService;
        this.userGroupOperator = userGroupOperator;
    }


    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${common.button.create}")
    @Operation(description = "create user group")
    @OperationLog(operateType = OperateType.CREATE, primaryField = "groupName")
    @PostMapping
    public R<UserSimpleGroup> createUserGroup(@RequestBody GroupForm groupForm){
        return R.success(userGroupOperator.createGroup(groupForm));
    }

    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${group.page.button.info}")
    @Operation(description = "update user group")
    @OperationLog(operateType = OperateType.UPDATE, primaryField = "groupId")
    @PutMapping
    public R<UserSimpleGroup> updateUserGroup(@RequestBody UserSimpleGroup simpleGroup){
        return R.success(userGroupOperator.saveGroup(simpleGroup,false));
    }

    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.button.delete}")
    @Operation(description = "delete user group")
    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{groupId}")
    public R<Boolean> deleteGroup(@PathVariable Integer groupId){
        return R.success(userGroupService.deleteGroup(groupId));
    }

    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${group.page.button.persons}")
    @Operation(description = "update user person group")
    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("{groupId}/users")
    public R<Boolean> updateUserPersonGroup(@PathVariable Integer groupId, @RequestParam List<Integer> userIds){
        return R.success(userGroupService.updateUserPersonGroup(userIds,groupId));
    }

    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${common.button.update}", label = "${group.page.button.header}")
    @Operation(description = "update group header user")
    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("{groupId}/header")
    public R<Boolean> updateGroupHeader(@PathVariable Integer groupId, @RequestParam String headerUserId){
        return R.success(userGroupService.updateGroupHeader(groupId,headerUserId));
    }


    @Operation(description = "list user group")
    @GetMapping("list")
    public R<List<UserGroup>> listUserGroups(){
        return R.success(userGroupService.listUserGroups());
    }

    @Operation(description = "list user by group id")
    @GetMapping("{groupId}/users")
    public R<List<SimpleUser>> listUserByGroupId(@PathVariable Integer groupId,
                                                 @RequestParam(required = false) Boolean includeSubGroup){
        return R.success(userGroupService.listUserByGroupId(groupId,includeSubGroup));
    }

}
