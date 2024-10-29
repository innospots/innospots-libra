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

package io.innospots.libra.kernel.module.function.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.script.function.definition.model.FunctionDefinition;
import io.innospots.base.model.field.FieldValueType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

/**
 * 函数展示项，单项展示
 *
 * @author Smars
 * @date 2021/8/29
 */
@Getter
@Setter
@Schema(title = "function view")
public class FunctionViewItem {

    @Schema(title = "function primary id")
    private Integer functionId;

    @Schema(title = "function name")
    private String name;

    @Schema(title = "expression value")
    private String value;

    @Schema(title = "function description")
    private String comment;

    @JsonInclude(NON_NULL)
    @Schema(title = "param types")
    private List<String> dataTypes;

    @JsonInclude(NON_NULL)
    private Boolean isChange;

    @JsonInclude(NON_NULL)
    @Schema(title = "multi params function list")
    private List<FunctionViewItem> valueList;

    public FunctionViewItem() {
    }

    public FunctionViewItem fill(FunctionDefinition fd) {
        this.name = fd.getName();
        this.value = fd.getExpression();
        this.comment = fd.getDescription();
        this.functionId = fd.getFunctionId();
        if (fd.getParamFieldTypes() != null) {
            dataTypes = new ArrayList<>();
            for (FieldValueType paramType : fd.getParamFieldTypes()) {
                dataTypes.add(paramType.name());
            }
        }

        if (fd.getMusts() != null &&
                fd.getMusts().stream().anyMatch(f -> !f)) {
            this.isChange = true;
            this.valueList = new ArrayList<>();
            String funcName = fd.getName().substring(0, fd.getName().indexOf("("));
            String[] pd = fd.getName().substring(fd.getName().indexOf("(") + 1, fd.getName().indexOf(")")).split(",");
            List<String> params = new ArrayList<>();
            for (int i = 0; i < fd.getMusts().size(); i++) {
                boolean m = fd.getMusts().get(i);
                params.add(pd[i].trim());
                m = !m || (i < fd.getMusts().size() - 1 && !fd.getMusts().get(i + 1));
                if (m) {
                    String fName = funcName + "(" + join(",", params) + ")";
                    String value = funcName + "(" + params.stream().map(p -> "${" + p + "}").collect(joining(",")) + ")";
                    FunctionViewItem viewItem = new FunctionViewItem();
                    viewItem.setName(fName);
                    viewItem.setValue(value);
                    viewItem.setDataTypes(dataTypes.subList(0, i + 1));
                    this.valueList.add(viewItem);
                }
            }
        }

        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", dataTypes=").append(dataTypes);
        sb.append(", isChange=").append(isChange);
        sb.append(", valueList=").append(valueList);
        sb.append('}');
        return sb.toString();
    }
}
