/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.libra.kernel.module.page.exporter;

import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.menu.BaseItem;
import io.innospots.libra.base.menu.DynamicMenuItemExporter;
import io.innospots.libra.base.menu.ItemType;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.base.category.BaseCategory;
import io.innospots.libra.kernel.module.page.operator.PageCategoryOperator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * export page category menu items, which will show in the navi bar
 *
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/19
 */
@Component
public class PageCategoryMenuItemExporter implements DynamicMenuItemExporter {


    private final PageCategoryOperator pageCategoryOperator;

    public PageCategoryMenuItemExporter(PageCategoryOperator pageCategoryOperator) {
        this.pageCategoryOperator = pageCategoryOperator;
    }

    @Override
    public List<ResourceItem> export(String itemKey) {
        List<BaseCategory> categories = pageCategoryOperator.listCategoryPages();
        List<ResourceItem> items = new ArrayList<>();
        for (BaseCategory category : categories) {
            ResourceItem resourceItem = new ResourceItem();
            resourceItem.setAppKey(appKey());
            /** sub menu item exporter */
            resourceItem.setItemKey(PageMenuItemExporter.MENU_KEY_PAGE_ITEM + "_" + category.getCategoryId());
            resourceItem.setLoadMode(ResourceItem.LoadMode.DYNAMIC);
            resourceItem.setItemType(ItemType.CATEGORY);
            resourceItem.setName(category.getCategoryName());
            resourceItem.setLabel(category.getCategoryName());
            resourceItem.setOpenMode(BaseItem.OpenMode.INTERNAL);
            resourceItem.setUri("/" + parentMenuKey() + "/" + category.getCategoryId());
            items.add(resourceItem);
        }
        return items;
    }

    /**
     * define in the META-INFO/innospots-extension-meta.json
     * menu key, which load mode is DYNAMIC
     *
     * @return
     */
    @Override
    public String parentMenuKey() {
        return "libra-page-category-dynamic";
    }

    @Override
    public String appKey() {
        return LibraClassPathExtPropertiesLoader.KERNEL_APP_KEY;
    }
}
