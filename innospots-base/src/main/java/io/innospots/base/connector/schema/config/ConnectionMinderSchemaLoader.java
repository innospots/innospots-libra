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

package io.innospots.base.connector.schema.config;

import io.innospots.base.exception.LoadConfigurationException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Raydian
 * @date 2021/1/27
 */
@Slf4j
public class ConnectionMinderSchemaLoader {

    private final static List<ConnectionMinderSchema> connectionMinderSchemas = new ArrayList<>();

    private ConnectionMinderSchemaLoader() {
    }

    static {
        initialize();
    }

    @PostConstruct
    public static void initialize() {
        reload();
    }

    public static synchronized void reload() {
        // A member variable is already initialized when it is defined
        connectionMinderSchemas.clear();

        /*
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:META-SOURCE/*.*");

            for (Resource resource : resources) {
                log.info("load credential form config:{}", resource.getFilename());
                InputStream inputStream = resource.getInputStream();
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

                ConnectionMinderSchema connectionMinderSchema = JSONUtils.parseObject(content, ConnectionMinderSchema.class);
                if (connectionMinderSchema != null) {
                    connectionMinderSchemas.add(connectionMinderSchema);
                }
            }

        } catch (Exception e) {
            throw LoadConfigurationException.buildException(ConnectionMinderSchemaLoader.class, e, "meta_source configuration load error");
        }
         */
        connectionMinderSchemas.sort(Comparator.comparing(ConnectionMinderSchema::getOrder).reversed());
    }

    public static List<ConnectionMinderSchema> connectionMinderSchemas() {
        return connectionMinderSchemas;
    }

    public static ConnectionMinderSchema getConnectionMinderSchema(String connectorName) {
        return connectionMinderSchemas.stream()
                .filter(f -> f.getName().equals(connectorName)).findFirst().orElse(null);
    }

    public static CredentialAuthOption getCredentialFormConfig(String connectorName, String configCode) {
        ConnectionMinderSchema connectionMinderSchema = getConnectionMinderSchema(connectorName);
        if (connectionMinderSchema == null) {
            throw LoadConfigurationException.buildException(ConnectionMinderSchemaLoader.class, "connection minder schema is empty");
        }
        if(configCode == null){
            return connectionMinderSchema.getOptions().stream().findFirst().orElse(null);
        }
        return connectionMinderSchema.getOptions().stream().filter(f -> f.getCode().equals(configCode)).findFirst()
                .orElseThrow(() -> LoadConfigurationException.buildException(ConnectionMinderSchemaLoader.class, "credential configuration can not found"));
    }

    public static CredentialAuthOption getCredentialFormConfig(String connectorName) {
        return getCredentialFormConfig(connectorName,null);
    }
}
