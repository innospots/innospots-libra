package io.innospots.app.console.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.app.core.entity.AppDefinitionEntity;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.BaseCategoryOperator;
import io.innospots.libra.base.category.CategoryType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/7
 */
public class AppDefinitionCategoryOperator extends BaseCategoryOperator {

    private final AppDefinitionOperator appDefinitionOperator;

    public AppDefinitionCategoryOperator(AppDefinitionOperator appDefinitionOperator) {
        this.appDefinitionOperator = appDefinitionOperator;
    }


    public List<BaseCategory> listCategories() {
        List<BaseCategory> categories = this.listCategories(CategoryType.APPS,
                () -> {
                    QueryWrapper<AppDefinitionEntity> qw = new QueryWrapper<>();
                    qw.lambda().select(AppDefinitionEntity::getCategoryId, AppDefinitionEntity::getAppKey)
                            .isNotNull(AppDefinitionEntity::getCategoryId);
                    List<AppDefinitionEntity> entries = this.appDefinitionOperator.list(qw);
                    return entries.stream().collect(Collectors.groupingBy(AppDefinitionEntity::getCategoryId, Collectors.counting()));
                }
        );
        categories.add(this.getRecycleBinCategory());
        return categories;
    }

}
