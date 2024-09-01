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

package io.innospots.base.connector.schema.model;

import io.innospots.base.constant.RegexConstants;
import io.innospots.base.model.BaseModelInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * the catalog in db schema
 *
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Getter
@Setter
@Schema(title = "schema catalog")
public class SchemaCatalog extends BaseModelInfo {

    @Size(max = 32, message = "name length max 64")
    @NotBlank(message = "Name cannot be blank")
    @Schema(title = "name")
    @Pattern(regexp = RegexConstants.NAME_REGEX, message = "name, only supports: contains a maximum of 32 characters, including letters, digits, and underscores (_), and chinese.")
    protected String name;

    @Schema(title = "description")
    protected String description;

    @Schema(title = "code")
    protected String code;

    protected Integer categoryId;

    @Schema(title = "credential key")
    protected String credentialKey;

    @Schema(title = "schema field list")
    protected List<SchemaField> schemaFields;

    @Schema(title = "application primary key")
    protected String appKey;

    @Schema(title = "app schema registry scope, example: application, general etc.")
    protected String scope;

    @Schema(title = "minder connector name")
    private String connectorName;

    @Schema(title = "minder auth options")
    private String authOption;

}
