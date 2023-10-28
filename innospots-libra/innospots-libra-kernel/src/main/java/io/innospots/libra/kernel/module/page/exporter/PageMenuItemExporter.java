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

import io.innospots.base.json.JSONUtils;
import io.innospots.base.data.body.PageBody;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.menu.BaseItem;
import io.innospots.libra.base.menu.DynamicMenuItemExporter;
import io.innospots.libra.base.menu.ItemType;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.module.page.model.Page;
import io.innospots.libra.kernel.module.page.operator.PageOperator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * export page category menu items, which will show in the navi bar
 *
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/19
 */
@Component
public class PageMenuItemExporter implements DynamicMenuItemExporter {

    public static final String MENU_KEY_PAGE_ITEM = "kernel-dashboard-page";

    private final PageOperator pageOperator;

    public PageMenuItemExporter(PageOperator pageOperator) {
        this.pageOperator = pageOperator;
    }

    @Override
    public List<ResourceItem> export(String keyId) {
        //MAX page
        PageBody<Page> pageBody = pageOperator.pagePages(Integer.parseInt(keyId), null, 0, 30);
        if (pageBody.pageSize() <= 0) {
            return Collections.emptyList();
        }
        List<ResourceItem> resourceItems = new ArrayList<>();
        for (Page page : pageBody.getList()) {
            ResourceItem resourceItem = new ResourceItem();
            Map<String, Object> config = JSONUtils.toMap(page.getBoardExtConfig());
            resourceItem.setItemType(ItemType.PAGE);
            resourceItem.setLabel((String) config.get("subName"));
            resourceItem.setName((String) config.get("name"));
            resourceItem.setItemKey("page_" + page.getId());
            resourceItem.setAppKey(appKey());
            resourceItem.setOpenMode(BaseItem.OpenMode.INTERNAL);
            resourceItem.setUri("/apps/visualization/" + page.getId());
            resourceItems.add(resourceItem);
        }

        return resourceItems;
    }

    /**
     * define in the META-INFO/innospots-extension-meta.json
     * menu key, which load mode is DYNAMIC
     *
     * @return
     */
    @Override
    public String parentMenuKey() {
        return MENU_KEY_PAGE_ITEM;
    }

    @Override
    public String appKey() {
        return LibraClassPathExtPropertiesLoader.KERNEL_APP_KEY;
    }
}
