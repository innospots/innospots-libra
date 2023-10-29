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

package io.innospots.base.connector.credential;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;

/**
 * @author Smars
 * @date 2021/2/15
 */
@Setter
@Getter
@Schema(title = "connection credential")
public class ConnectionCredential {

    @Schema(title = "credential primary key")
    private String credentialKey;

    @Schema(title = "credential type code")
    private String credentialTypeCode;

    @Schema(title = "auth option in the CredentialType")
    private String authOption;

    @Schema(title = "connector name")
    private String connectorName;

    @Schema(title = "config")
    private Map<String, Object> config;

    @Schema(title = "props")
    private Map<String,String> props;

    public String key() {
        return valueOf(credentialKey);
    }

    public String prop(String key){
        if(props==null){
            return null;
        }
        return props.get(key);
    }

    public Object value(String key) {
        if (config == null) {
            return null;
        }
        return config.get(key);
    }

    public String v(String key) {
        if (config == null) {
            return null;
        }
        Object vv = config.get(key);
        if(vv==null){
            return null;
        }
        return String.valueOf(vv);
    }

    public String v(String key, String defaultValue) {
        if (config == null) {
            return null;
        }
        return String.valueOf(config.getOrDefault(key, defaultValue));
    }

    public void config(String key, String value) {
        if(this.config == null){
            this.config = new HashMap<>();
        }
        this.config.put(key,value);
    }

    public void config(Map<String,Object> props){
        if(this.config == null){
            this.config = new HashMap<>();
        }
        this.config.putAll(props);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionCredential{");
        sb.append("credentialKey='").append(credentialKey).append('\'');
        sb.append(", credentialTypeCode='").append(credentialTypeCode).append('\'');
        sb.append(", connectorName='").append(connectorName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
