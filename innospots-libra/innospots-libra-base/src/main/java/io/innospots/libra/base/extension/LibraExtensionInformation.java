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

package io.innospots.libra.base.extension;

import io.innospots.base.i18n.LocaleMessageUtils;
import io.innospots.base.json.annotation.I18n;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/12/1
 */
@Setter
@Getter
public class LibraExtensionInformation {

    @I18n
    protected String name;

    protected String author;

    protected String icon;

    protected String version;

    protected String publishTime;

    protected String vendor;

    protected String extKey;

    protected String kernelVersion;

    protected String description;

    protected String[] basePackages;

    protected ExtensionStatus status;

    protected Boolean compatible;

    protected Map<String, String> i18nNames;

    protected String lastVersion;

    protected String lastPublishTime;

    protected String lastDescription;

    protected String[] endpoints;

    protected String[] indicators;

    /**
     * start: [start.sh, start.cmd]
     * stop: [stop.sh,stop.cmd]
     * restart: [restart.sh,restart.cmd]
     */
    protected Map<String, List<String>> commands;

    private ExtensionSource extensionSource;

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


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", publishTime='").append(publishTime).append('\'');
        sb.append(", vendor='").append(vendor).append('\'');
        sb.append(", extKey='").append(extKey).append('\'');
        sb.append(", kernelVersion='").append(kernelVersion).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", basePackages=").append(basePackages == null ? "null" : Arrays.asList(basePackages).toString());
        sb.append(", status=").append(status);
        sb.append(", compatible=").append(compatible);
        sb.append(", i18nNames=").append(i18nNames);
        sb.append(", lastVersion='").append(lastVersion).append('\'');
        sb.append(", lastPublishTime='").append(lastPublishTime).append('\'');
        sb.append(", lastDescription='").append(lastDescription).append('\'');
        sb.append(", extensionSource=").append(extensionSource);
        sb.append('}');
        return sb.toString();
    }
}
