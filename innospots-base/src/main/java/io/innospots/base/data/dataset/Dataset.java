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

package io.innospots.base.data.dataset;

import io.innospots.base.constant.RegexConstants;
import io.innospots.base.model.BaseModelInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Alfred
 * @date 2022/1/27
 */
@Getter
@Setter
@Schema(title = "data set")
public class Dataset extends BaseModelInfo {

    @Schema(title = "dataset id") // registryId
    private Integer id;

    @Schema(title = "category id")
    private Integer categoryId;

//    @Schema(title = "registry id")
//    private Integer registryId;

    @NotNull(message = "credentialKey cannot be empty")
    @Schema(title = "credential key")
    protected String credentialKey;

    @Schema(title = "dataset code")
    private String code;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 32, message = "name length max 32")
    @Schema(title = "name")
    @Pattern(regexp = RegexConstants.NAME_REGEX, message = "name, only supports: contains a maximum of 32 characters, including letters, digits, and underscores (_), and chinese.")
    protected String name;

    @NotNull(message = "script cannot be empty")
    @Schema(title = "script")
    private String script; // 转换为SchemaRegistry.script

    @Schema(title = "model") // config
    private String model;

//    @Schema(title = "description")
//    protected String description;

    @Schema(title = "scriptVariables")
    private List<Variable> variables;

    @Schema(title = "icon")
    private String icon;

}
