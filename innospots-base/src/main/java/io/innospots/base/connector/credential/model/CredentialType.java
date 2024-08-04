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

package io.innospots.base.connector.credential.model;

import io.innospots.base.model.BaseModelInfo;
import io.innospots.base.model.PBaseModelInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.*;

/**
 * credential type
 *
 * @author Smars
 * @date 2023/10/28
 */
@Getter
@Setter
@Schema(title = "credential type")
public class CredentialType extends PBaseModelInfo {

    @Schema(title = "unique code")
    private String typeCode;

    @Size(max = 32, message = "name length max 32")
    @NotBlank(message = "Name cannot be blank")
    @Schema(title = "credential name")
    private String name;

    @Schema(title = "icon url")
    private String icon;

    @Schema(title = "connector auth option in form")
    private String authOption;

    @NotBlank(message = "connector name cannot be blank")
    @Schema(title = "connector name")
    private String connectorName;

    /**
     * credential auth form value
     * readOnlies
     */
    @Schema(title = "formValues")
    private List<FormValue> formValues = new ArrayList<>();

    @Schema(title = "props")
    private Map<String, Object> props;
}
