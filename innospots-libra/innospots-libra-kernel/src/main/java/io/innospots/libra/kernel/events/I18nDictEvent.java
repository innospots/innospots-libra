/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.libra.kernel.events;

import io.innospots.base.events.EventBody;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * create national i18n dictionary
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/18
 */
public class I18nDictEvent extends EventBody {

    private String extensionName;

    private String module;

    private String code;

    private Map<String, String> i18nNames;

    public I18nDictEvent(String extensionName, String module, String code) {
        super(code);
        this.extensionName = extensionName;
        this.module = module;
        this.code = code;
    }

    public I18nDictEvent(String extensionName, String module, String code, Map<String, String> i18nNames) {
        super(code);
        this.extensionName = extensionName;
        this.module = module;
        this.code = code;
        this.i18nNames = i18nNames;
    }

    public I18nDictEvent(String source) {
        super(source);
    }

    public String getExtensionName() {
        return extensionName;
    }

    public String getModule() {
        return module;
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getI18nNames() {
        return i18nNames;
    }

    public void setI18nNames(Map<String, String> i18nNames) {
        this.i18nNames = i18nNames;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("I18nDictEvent{");
        sb.append("extensionName='").append(extensionName).append('\'');
        sb.append(", module='").append(module).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", i18nNames=").append(i18nNames);
        sb.append('}');
        return sb.toString();
    }
}
