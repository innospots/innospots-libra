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

package io.innospots.libra.base.category;

import io.innospots.base.constant.Constants;
import io.innospots.base.model.PBaseModelInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Alfred
 * @date 2022/2/10
 */
@Getter
@Setter
public class BaseCategory extends PBaseModelInfo {

    @Schema(title = "category id")
    private Integer categoryId;

    @Schema(title = "parent category")
    private Integer parentCategoryId;

    @Schema(title = "category code")
    private String categoryCode;

    @Size(max = 16, message = "category name length max 16")
    @NotBlank(message = "category name must not be null")
    @Schema(title = "category name")
    @Pattern(regexp = Constants.NAME_REGEX, message = "name, only supports: contains a maximum of 32 characters, including letters, digits, and underscores (_), and chinese.")
    private String categoryName;

    @Schema(title = "subset total")
    private Integer totalCount = 0;

    @Schema(title = "children")
    private List<BaseCategory> children;
}
