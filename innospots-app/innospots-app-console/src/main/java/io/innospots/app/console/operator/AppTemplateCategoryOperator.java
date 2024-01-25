package io.innospots.app.console.operator;

import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.base.category.BaseCategoryOperator;
import io.innospots.libra.base.category.CategoryType;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/7
 */
public class AppTemplateCategoryOperator extends BaseCategoryOperator {

    public List<BaseCategory> listCategories() {
        return this.listCategories(CategoryType.APP_TPL);
    }

}
