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

package io.innospots.libra.kernel.module.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import io.innospots.libra.base.menu.BaseItem;
import io.innospots.libra.base.menu.ItemType;
import io.innospots.libra.base.menu.OptElement;
import io.innospots.libra.base.menu.ResourceItem;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import static io.innospots.libra.kernel.module.menu.entity.MenuResourceEntity.TABLE_NAME;

/**
 * @author Raydian
 * @date 2021/1/17
 */
@Getter
@Setter
@Entity
@TableName(value = TABLE_NAME)
@Table(name = TABLE_NAME)
public class MenuResourceEntity extends PBaseEntity {

    public static final String TABLE_NAME = "sys_menu_resource";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resourceId;

    @Column(length = 64)
    private String name;

    /**
     * menu tree group, which includes all the items of the menu
     * different groups can be used to different modules or pages.
     * the "system" group is default menu group in the libra.
     */
    @Column(length = 16)
    private String menuGroup;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private BaseItem.OpenMode openMode;

    @Column
    private Boolean showMenu;

    @Column
    private Integer parentId;

    @Column
    private Integer orders;

    @Column(length = 128)
    private String uri;

    @Column(length = 32)
    private String icon;

    @Column(length = 32)
    @Enumerated(value = EnumType.STRING)
    private OptElement.UriMethod method;

    @Column(length = 64)
    private String itemKey;

    @Column(length = 64)
    private String appKey;

    @Column(length = 128)
    private String appName;

    @Column(length = 128)
    private String parentItemKeys;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ItemType itemType;

    @Column(length = 16)
    @Enumerated(value = EnumType.STRING)
    private ResourceItem.LoadMode loadMode;

    @Column
    private Boolean status;

    @Column(columnDefinition = "TEXT")
    private String i18nNames;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("resourceId=").append(resourceId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", menuGroup='").append(menuGroup).append('\'');
        sb.append(", openMode=").append(openMode);
        sb.append(", showMenu=").append(showMenu);
        sb.append(", parentId=").append(parentId);
        sb.append(", orders=").append(orders);
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append(", method=").append(method);
        sb.append(", itemKey='").append(itemKey).append('\'');
        sb.append(", itemType=").append(itemType);
        sb.append(", status=").append(status);
        sb.append(", i18nNames='").append(i18nNames).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
