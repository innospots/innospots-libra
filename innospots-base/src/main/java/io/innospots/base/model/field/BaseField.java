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

import io.innospots.base.constant.RegexConstants;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Setter
@Getter
public class BaseField {

    protected Integer fieldId;

    @Size(max = 32, message = "Name length max 32")
    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = RegexConstants.NAME_REGEX, message = "name, only supports: contains a maximum of 32 characters, including letters, digits, and underscores (_), and chinese.")
    protected String name;

    @Size(max = 64, message = "Code length max 64")
    @NotBlank(message = "Code cannot be blank")
    @Pattern(regexp = RegexConstants.CODE_REGEX, message = "code, only supports: contains a maximum of 64 characters, including letters, digits, and underscores (_), and chinese. It can't all be numbers.")
    protected String code;

    @NotNull(message = "ValueType cannot be empty")
    protected FieldValueType valueType;

    /**
     * field comment information
     */
    protected String comment;

}
