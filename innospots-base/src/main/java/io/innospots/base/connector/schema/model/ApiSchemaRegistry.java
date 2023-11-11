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

package io.innospots.base.connector.schema.model;

import io.innospots.base.data.enums.ApiMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Alfred
 * @date 2022/4/28
 */
@Getter
@Setter
@Schema(title = "api schema registry")
public class ApiSchemaRegistry extends SchemaCatalog {

    @Schema(title = "schema registry id")
    protected Integer registryId;

    @NotBlank(message = "address cannot be blank")
    @Schema(title = "address")
    private String address;

    @NotNull(message = "Api method be blank")
    @Schema(title = "api method")
    private ApiMethod apiMethod;

    @Schema(title = "body template")
    private String bodyTemplate;

    @Schema(title = "previous script")
    private String prevScript;

    @Schema(title = "post script")
    private String postScript;

    @Schema(title = "param value")
    private Map<String, Object> paramValue;

}
