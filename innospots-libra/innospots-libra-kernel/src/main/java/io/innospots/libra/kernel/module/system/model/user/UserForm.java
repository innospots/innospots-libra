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

package io.innospots.libra.kernel.module.system.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.constant.RegexConstants;
import io.innospots.base.enums.OnOff;
import io.innospots.base.json.annotation.MaskValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * @author Raydian
 * @date 2020/12/19
 */
@Getter
@Setter
@Schema(title = "UserForm")
public class UserForm {

    protected Integer userId;

    @NotNull(message = "real name must not be null")
    @Size(max = 128, message = "realName length max 128")
    protected String realName;

    @NotNull(message = "user name must not be null")
    @Pattern(regexp = RegexConstants.EN_NUM, message = "illegal user name")
    @Size(max = 128, message = "real name length max 128")
    protected String userName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String avatarBase64;

    protected String avatarKey;

    @NotNull(message = "email must not be null")
    @Pattern(regexp = RegexConstants.REXP_EMAIL, message = "illegal email format")
    @Size(max = 128, message = "email length max 128")
    protected String email;

    @MaskValue
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String password;

    @Size(max = 32, message = "mobile length max 32")
    @Pattern(regexp = RegexConstants.REXP_PHONE, message = "illegal mobile")
    protected String mobile;

    protected String department;

    @NotEmpty(message = "role must not be null")
    protected List<Integer> roleIds;

    protected String remark;

    protected OnOff onOff;

}