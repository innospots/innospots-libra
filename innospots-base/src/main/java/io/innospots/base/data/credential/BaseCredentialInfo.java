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

package io.innospots.base.data.credential;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.constant.RegexConstants;
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
 * @author Smars
 * @version 1.0.0
 * @date 2023/1/19
 */
@Getter
@Setter
public class BaseCredentialInfo {

    @Schema(title = "credential primary key")
    protected Integer credentialKey;

    @NotBlank(message = "credential type code cannot be blank")
    @Schema(title = "credential type code")
    protected String credentialTypeCode;

    @NotBlank(message = "connector name can't be blank")
    @Schema(title = "connector name")
    protected String connectorName;

    /**
     * credential config form
     */
    @Schema(title = "formValues")
    @JsonIgnore
    protected Map<String, Object> formValues = new LinkedHashMap<>();

    @Schema(title = "props")
    protected Map<String,Object> props;
}
