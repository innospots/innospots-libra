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

package io.innospots.libra.base.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.json.annotation.I18n;
import io.innospots.base.i18n.LocaleMessageUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/18
 */
@Getter
@Setter
public class BaseItem {


    @Schema(title = "primary key")
    protected Integer resourceId;

    protected BaseItem.LoadMode loadMode;

    protected String itemKey;
    @I18n
    @NotNull(message = "name must not be null")
    protected String name;
    @I18n
    private String label;

    @NotNull(message = "icon must not be null")
    protected String icon;
    protected String uri;
    protected ItemType itemType;
    protected OpenMode openMode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Map<String, String> i18nNames;

    @Schema(title = "the key of the extension in which menu is defined")
    protected String appKey;

    protected boolean adminDefault;

    protected ViewScope viewScope;


    public void fillI18n() {
        Locale locale = LocaleMessageUtils.getLocale();
        String localeKey = locale.getLanguage() + "_" + locale.getCountry();
        if (MapUtils.isNotEmpty(i18nNames)) {
            String s = i18nNames.get(localeKey);
            if (s != null) {
                this.name = s;
            } else {
                localeKey = Locale.ENGLISH.getLanguage() + "_" + Locale.ENGLISH.getCountry();
                this.name = i18nNames.get(localeKey);
            }
        }
    }

    public enum OpenMode {
        /**
         *
         */
        INTERNAL,
        NEW_PAGE;
    }


    /**
     *
     */
    public enum LoadMode {
        /**
         *
         */
        CONFIG,
        DYNAMIC;
    }

    public enum ViewScope {
        V_PUBLIC,
        V_ROLE,
        V_CATEGORY,
        V_USER;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("itemKey='").append(itemKey).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", itemType=").append(itemType);
        sb.append(", openMode=").append(openMode);
        sb.append('}');
        return sb.toString();
    }

}
