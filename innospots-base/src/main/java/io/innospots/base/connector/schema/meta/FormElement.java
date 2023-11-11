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

package io.innospots.base.connector.schema.meta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Raydian
 * @date 2021/1/17
 */
@Getter
@Setter
public class FormElement implements Cloneable {

    @Schema(title = "form element label name, if component is checkbox or select，label is null")
    private String label;

    @Schema(title = "form element name")
    private String name;

    @Schema(title = "form element value")
    private String value;

    @Schema(title = "form element icon")
    private String icon;

    @Schema(title = "form element placeholder")
    private String placeholder;

    @Schema(title = "required set")
    private Boolean required;

    @Schema(title = "The number of label grids occupied，1 or 2")
    private Integer labelGrid;

    @Schema(title = "tips info")
    private String tips;

    private ElementType type;

    private String expression;

    @Schema(title = "form value encrypt to credential")
    private Boolean encrypt;

    @Schema(title = "checkbox list or select list data")
    private List<LinkedHashMap<String, Object>> options;

    @Schema(title = "The number of grids occupied，1 or 2")
    private Integer gridSize;

    @Schema(title = "value not be modified")
    private boolean readOnly;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("label='").append(label).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append(", placeholder='").append(placeholder).append('\'');
        sb.append(", required=").append(required);
        sb.append(", labelGrid=").append(labelGrid);
        sb.append(", tips='").append(tips).append('\'');
        sb.append(", type=").append(type);
        sb.append(", expression='").append(expression).append('\'');
        sb.append(", encrypt=").append(encrypt);
        sb.append(", options=").append(options);
        sb.append(", gridSize=").append(gridSize);
        sb.append(", readOnly=").append(readOnly);
        sb.append('}');
        return sb.toString();
    }
}
