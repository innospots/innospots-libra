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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/9
 */
@Getter
@Setter
@Schema(title = "i18 language")
public class I18nLanguage {

    @Schema(title = "primary id")
    private Integer languageId;

    @NotNull(message = "language name not null")
    @Size(max = 32, message = "language name max length 32")
    @Schema(title = "language name")
    private String name;

    @NotNull(message = "language locale not null")
    @Size(max = 32, message = "language locale max length 32")
    @Schema(title = "language locale code, @see java.util.Locale")
    private String locale;

    @Schema(title = "national flag icon")
    private String icon;

    @NotNull(message = "language currency not null")
    @Schema(title = "currency primary id")
    private Integer currencyId;

    @Schema(title = "currency name that display in the panel")
    private String displayCurrency;

    @NotNull(message = "currency decimal separator not null")
    @Size(max = 4, message = "currency decimal separator max length 4")
    @Schema(title = "decimal separator")
    private String decimalSeparator;

    @NotNull(message = "currency thousands separator not null")
    @Size(max = 4, message = "currency thousands separator max length 4")
    @Schema(title = "thousands separator")
    private String thousandSeparator;

    @Schema(title = "format the date that show in the panel, ex: yyyy-MM-dd")
    private String dateFormat;

    @Schema(title = "format the time that show in the panel, ex: HH:mm")
    private String timeFormat;

    @Schema(title = "is used to default language")
    private boolean defaultLan;

    @NotNull(message = "language status not null")
    @Schema(title = "data switch on-off")
    private DataStatus status;

}
