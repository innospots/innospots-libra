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

package io.innospots.base.data.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.constant.Constants;
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
public class AppCredentialInfo extends BaseModelInfo {

    @Schema(title = "credential id")
    private Integer credentialId;

    @Size(max = 32, message = "name length max 32")
    @NotBlank(message = "Name cannot be blank")
    @Schema(title = "name")
    @Pattern(regexp = Constants.NAME_REGEX, message = "name, only supports: contains a maximum of 32 characters, including letters, digits, and underscores (_), and chinese.")
    private String name;

    @Schema(title = "identity code")
    private String code;

    @NotBlank(message = "config code cannot be blank")
    @Schema(title = "config code")
    private String configCode;

    @NotBlank(message = "connector name can't be blank")
    @Schema(title = "connector name")
    private String connectorName;

    @Size(max = 128, message = "description length max 16")
    @Schema(title = "description")
    private String description;

    /**
     * server side usage
     */
    @Schema(title = "formValues")
    @JsonIgnore
    private Map<String, Object> formValues = new LinkedHashMap<>();

    @Schema(title = "encrypt json string  form values")
    private String encryptFormValues;

    @Schema(title = "props")
    private Map<String,Object> props;

//    @Schema(title = "authed values")
//    @JsonIgnore
//    private String authedValues;

    @Schema(title = "app node code")
    private String appNodeCode;

}
