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

package io.innospots.base.model.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author chenc
 * @date 2021/2/7 17:08
 */
@Getter
@Setter
@Schema(title = "Organization Information")
public class OrganizationInfo {

    @Schema(title = "Organization ID")
    protected Integer orgId;

    @NotNull(message = "organization name must not be null")
    @Size(max = 32, message = "organization name length max 32")
    @Schema(title = "Organization Name")
    protected String orgName;

    @NotNull(message = "organization code must not be null")
    @Size(max = 32, message = "organization code length max 32")
    @Schema(title = "Organization code")
    protected String orgCode;

    @NotNull(message = "organization default language must not be null")
    @Schema(title = "default organization language")
    protected String defaultLanguage;

    @NotNull(message = "organization default locale must not be null")
    @Schema(title = "default organization locale")
    protected String defaultLocale;

    @NotNull(message = "organization default currency must not be null")
    @Schema(title = "default organization currency")
    protected String defaultCurrency;

    @NotNull(message = "organization icon must not be null")
    @Schema(title = "fav icon image base64 or url")
    protected String favIcon;

    @NotNull(message = "organization logo must not be null")
    @Schema(title = "org logo image base64 or url")
    protected String logo;
}
