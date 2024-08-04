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

package io.innospots.libra.kernel.module.i18n.model;

import io.innospots.base.enums.DataStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Smars
 * @date 2021/12/20
 */
@Getter
@Setter
@Schema(title = "i18n currency model")
public class I18nCurrency {

    @Schema(title = "currency primary id")
    private Integer currencyId;

    @NotNull(message = "currency name not null")
    @Size(max = 16, message = "currency name max length 16")
    @Schema(title = "currency name")
    private String name;
    @NotNull(message = "currency code not null")
    @Size(max = 16, message = "currency code max length 16")
    @Schema(title = "national currency code , ISO 4217")
    private String code;
    @Size(max = 4, message = "currency leftSign max length 4")
    @Schema(title = "the sign in the left of the currency amount")
    private String leftSign;
    @Size(max = 4, message = "currency rightSign max length 4")
    @Schema(title = "the sign in the right of the currency amount")
    private String rightSign;

    @NotNull(message = "currency decimalDigits not null")
    @Min(value = 1, message = "currency decimalDigits max 1")
    @Max(value = 10, message = "currency decimalDigits max 10")
    @Schema(title = "decimal digits")
    private Integer decimalDigits;

    @Schema(title = "switch ONLINE OFFLINE")
    private DataStatus status;

    @Schema(title = "currency sample")
    private String sampleDisplay;

}
