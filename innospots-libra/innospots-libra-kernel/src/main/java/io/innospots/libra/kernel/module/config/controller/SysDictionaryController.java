package io.innospots.libra.kernel.module.config.controller;

import io.innospots.base.model.response.R;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.libra.kernel.module.config.model.SysDictionary;
import io.innospots.libra.kernel.module.config.model.SysDictionaryType;
import io.innospots.libra.kernel.module.config.operator.SysDictionaryOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/26
 */
@Tag(name = "System Dictionary")
@RestController
@RequestMapping(BaseController.PATH_ROOT_ADMIN + "dictionary")
@ModuleMenu(menuKey = "libra-system-dictionary")
public class SysDictionaryController {

    private final SysDictionaryOperator sysDictionaryOperator;

    public SysDictionaryController(SysDictionaryOperator sysDictionaryOperator) {
        this.sysDictionaryOperator = sysDictionaryOperator;
    }

    @Operation(summary = "Query Dictionary List")
    @GetMapping("list/{type}")
    public R<List<SysDictionary>> listDictionaries(@PathVariable String type) {
        return R.success(sysDictionaryOperator.list(type));
    }

    @Operation(summary = "Save Dictionary")
    @PostMapping("save/item")
    public R<Boolean> saveDictionary(@RequestBody SysDictionary sysDictionary) {
        return R.success(sysDictionaryOperator.save(sysDictionary));
    }

    @Operation(summary = "Save Dictionary List")
    @PostMapping("save/list")
    public R<Boolean> saveDictionary(@RequestBody List<SysDictionary> sysDictionaries) {
        return R.success(sysDictionaryOperator.save(sysDictionaries));
    }

    @Operation(summary = "Delete Dictionary")
    @PostMapping("delete-item/{type}")
    public R<Boolean> deleteDictionary(@PathVariable String type, @RequestParam String code) {
        return R.success(sysDictionaryOperator.delete(type, code));
    }

    @Operation(summary = "Delete Dictionary Type")
    @PostMapping("delete-type/{type}")
    public R<Boolean> deleteDictionary(@PathVariable String type) {
        return R.success(sysDictionaryOperator.delete(type));
    }

    @GetMapping("list-types")
    @Operation(summary = "Query Dictionary Type List")
    public R<List<SysDictionaryType>> listTypes() {
        return R.success(sysDictionaryOperator.listTypes());
    }

}
