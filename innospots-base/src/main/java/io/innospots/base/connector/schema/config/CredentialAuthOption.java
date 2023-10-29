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

package io.innospots.base.connector.schema.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Raydian
 * @date 2021/1/17
 */
@Getter
@Setter
public class CredentialAuthOption implements Cloneable {

    private String name;

    private String code;

    private String minder;

    private LinkedHashMap<String, String> defaults;

    private List<FormElement> elements;

    @Override
    public Object clone() throws CloneNotSupportedException {
        CredentialAuthOption cloneObj = (CredentialAuthOption) super.clone();

        if (this.defaults != null) {
            LinkedHashMap<String, String> dfs = new LinkedHashMap<>();
            dfs.putAll(this.defaults);
            cloneObj.defaults = dfs;
        }
        if (this.elements != null) {
            List<FormElement> newElements = new ArrayList<>();
            for (FormElement element : this.elements) {
                newElements.add((FormElement) element.clone());
            }
            cloneObj.elements = newElements;
        }
        return cloneObj;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("code='").append(code).append('\'');
        sb.append(", defaults=").append(defaults);
        sb.append(", elements=").append(elements);
        sb.append('}');
        return sb.toString();
    }
}
