package io.innospots.app.console.controller;

import io.innospots.app.console.operator.AppDefinitionCategoryOperator;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.CategoryType;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.log.OperateType;
import io.innospots.libra.base.log.OperationLog;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.base.menu.ResourceItemOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;
import static io.innospots.libra.base.menu.ItemType.BUTTON;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/7
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "app/definition/category")
@ModuleMenu(menuKey = "applications")
@Tag(name = "application category")
public class AppDefinitionCategoryController extends BaseController {

    private final AppDefinitionCategoryOperator appDefinitionCategoryOperator;

    public AppDefinitionCategoryController(AppDefinitionCategoryOperator appDefinitionCategoryOperator) {
        this.appDefinitionCategoryOperator = appDefinitionCategoryOperator;
    }


    @OperationLog(operateType = OperateType.CREATE, idParamPosition = 0)
    @PostMapping
    @Operation(summary = "create application category")
    @ResourceItemOperation(type = BUTTON, icon = "create", name = "${app.category.add.title}")
    public InnospotsResponse<BaseCategory> createCategory(@Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        BaseCategory category = appDefinitionCategoryOperator.createCategory(categoryName, CategoryType.APPS);
        return success(category);
    }

    @OperationLog(operateType = OperateType.UPDATE, idParamPosition = 0)
    @PutMapping("{categoryId}")
    @Operation(summary = "update application category")
    @ResourceItemOperation(type = BUTTON, icon = "update", name = "${page.category.edit.title}")
    public InnospotsResponse<Boolean> updateCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId,
                                                     @Parameter(required = true, name = "categoryName") @RequestParam("categoryName") String categoryName) {
        Boolean update = appDefinitionCategoryOperator.updateCategory(categoryId, categoryName, CategoryType.APPS);
        return success(update);
    }

    @OperationLog(operateType = OperateType.DELETE, idParamPosition = 0)
    @DeleteMapping("{categoryId}")
    @Operation(summary = "delete application category")
    @ResourceItemOperation(type = BUTTON, icon = "delete", name = "${common.category.delete.title}")
    public InnospotsResponse<Boolean> deleteCategory(@Parameter(required = true, name = "categoryId") @PathVariable Integer categoryId) {
        return success(appDefinitionCategoryOperator.deleteCategory(categoryId));
    }


    @GetMapping
    @Operation(summary = "application category list")
    public InnospotsResponse<List<BaseCategory>> listCategories() {
        List<BaseCategory> list = appDefinitionCategoryOperator.listCategories();
        return success(list);

    }

//    @GetMapping("check/{categoryName}")
//    @Operation(summary = "check name duplicate", description = "return: true = duplicate,false = not duplicate")
//    public InnospotResponse<Boolean> checkNameExist(@Parameter(required = true, name = "categoryName") @PathVariable String categoryName) {
//        return success(appDefinitionCategoryOperator.checkNameExist(categoryName, CategoryType.APPS));
//    }


}
