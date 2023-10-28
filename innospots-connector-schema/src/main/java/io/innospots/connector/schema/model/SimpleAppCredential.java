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

package io.innospots.connector.schema.model;

import io.innospots.base.enums.ConnectType;
import io.innospots.base.model.BaseModelInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Alfred
 * @date 2023/4/22
 */
@Getter
@Setter
public class SimpleAppCredential extends BaseModelInfo {

    @Schema(title = "credential id")
    private Integer credentialId;

    @Schema(title = "name")
    private String name;

    @Schema(title = "schema connector type")
    private ConnectType connectType;

    @Schema(title = "icon")
    private String icon;

    public SimpleAppCredential() {
    }

    public SimpleAppCredential(Integer credentialId, String name, ConnectType connectType) {
        this.credentialId = credentialId;
        this.name = name;
        this.connectType = connectType;
    }
}
