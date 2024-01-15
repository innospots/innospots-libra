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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Getter
@Setter
@Schema(title = "schema registry")
public class SchemaRegistry extends SchemaCatalog {

    public static final String QUEUE_CONFIG_MESSAGE_FORMAT = "message.format";
    public static final String QUEUE_CONFIG_SCAN_STARTUP_MODE = "scan.startup.mode";

    @Schema(title = "schema registry primaryKey")
    protected String registryId;

    @NotNull(message = "registryType cannot be empty")
    @Schema(title = "registryType type")
    protected SchemaRegistryType registryType;

    @Schema(title = "configs")
    protected Map<String, Object> configs;

    @Schema(title = "json body include scripts")
    protected Map<String, Object> script;

    public void addScript(String scriptKey, Object body) {
        if (script == null) {
            script = new HashMap<>();
        }
        script.put(scriptKey, body);
    }

    public void addConfig(String configKey, Object value) {
        if (configs == null) {
            configs = new HashMap<>();
        }
        this.configs.put(configKey, value);
    }

    public Object configValue(String configKey) {
        if (configs != null) {
            return configs.get(configKey);
        }
        return null;
    }

    public Object scriptValue(String configKey) {
        if (script != null) {
            return script.get(configKey);
        }
        return null;
    }

}
