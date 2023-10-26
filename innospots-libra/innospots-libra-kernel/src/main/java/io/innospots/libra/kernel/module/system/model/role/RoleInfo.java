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

package io.innospots.libra.kernel.module.system.model.role;

import io.innospots.base.constant.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author chenc
 * @date 2021/6/26 14:33
 */
@Getter
@Setter
@Schema(title = "Role info")
public class RoleInfo {
    private Integer roleId;

    @NotNull(message = "role name must not be null")
    @Size(max = 32, message = "role name is too long")
    @Pattern(regexp = RegexConstants.CN_EN_NUM, message = "illegal role name")
    private String roleName;

    @NotNull(message = "role code must not be null")
    @Size(max = 32, message = "role code is too long")
    @Pattern(regexp = RegexConstants.EN_NUM, message = "illegal role code")
    private String roleCode;

    private boolean admin;

    private Integer numberOfRole;
}