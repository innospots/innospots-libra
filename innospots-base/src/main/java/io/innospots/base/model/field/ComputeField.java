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

package io.innospots.base.model.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.model.field.compute.ComputeItem;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.aviator.AviatorScriptExecutor;
import io.innospots.base.utils.Initializer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 计算字段变量
 *
 * @author Smars
 * @date 2021/8/22
 */
@Getter
@Setter
@Slf4j
public class ComputeField extends BaseField implements Initializer {


    /**
     * 计算公式表达式项
     */
    private List<ComputeItem> computeItems;

    @JsonIgnore
    private IScriptExecutor expression;

    private String expr;

    public Object compute(Map<String, Object> env) {
        return expression.execute(env);
    }


    public String toExpScript(String scriptType) {
        if (computeItems == null) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        for (ComputeItem computeItem : computeItems) {
            out.append(computeItem.output(scriptType));
        }
        return out.toString();
    }

    @Override
    public void initialize() {
        expr = toExpScript("aviator");
        log.debug("field,{},{} expression:{},", code, name, expr);
//        expression = new AviatorScriptExecutor(expr, null);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", valueType=").append(valueType);
        sb.append(", expr='").append(expr).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
