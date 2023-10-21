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

package io.innospots.workflow.core.node.apps;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/5/9
 */
@Getter
@Setter
@Schema(title = "app connector credential config")
public class AppConnectorConfig {

    @Schema(title = "tab and checkbox value")
    private String configCode;

    @Schema(title = "tab and checkbox label")
    private String configName;

    @Schema(title = "connector credential ")
    private Map<String,Object> formValues;

    private Map<String,Object> props;

    public Object getValue(String name){
        if(formValues!=null){
            return formValues.get(name);
        }
        return null;
    }

    public boolean isReadOnly(String name) {
        Object rv = getValue("readOnly");
        if(rv instanceof Map){
            Map<String,Boolean> ro = (Map<String, Boolean>) rv;
            return ro.getOrDefault(name,Boolean.FALSE);
        }
        return false;
    }

}
