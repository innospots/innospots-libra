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

package io.innospots.libra.kernel.module.credential.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.constant.RegexConstants;
import io.innospots.base.enums.ConnectType;
import io.innospots.base.model.BaseModelInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * simple credential info not include form value and props
 * @author Alfred
 * @date 2023/4/22
 */
@Getter
@Setter
public class SimpleCredentialInfo extends BaseModelInfo {


    @Schema(title = "credential primary key")
    protected Integer credentialKey;

    @Size(max = 32, message = "name length max 32")
    @NotBlank(message = "Name cannot be blank")
    @Schema(title = "name")
    @Pattern(regexp = RegexConstants.NAME_REGEX, message = "name, only supports: contains a maximum of 32 characters, including letters, digits, and underscores (_), and chinese.")
    protected String name;

    @NotBlank(message = "credential type code cannot be blank")
    @Schema(title = "credential type code")
    protected String credentialTypeCode;

    @NotBlank(message = "connector name can't be blank")
    @Schema(title = "credential type icon's connector name")
    protected String connectorName;

    @Schema(title = "credential type icon")
    private String icon;

}
