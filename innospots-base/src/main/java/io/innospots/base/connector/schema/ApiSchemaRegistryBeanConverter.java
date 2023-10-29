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

package io.innospots.base.connector.schema;

import io.innospots.base.data.enums.ApiMethod;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.innospots.base.connector.http.HttpConstant.*;


/**
 * @author Alfred
 * @date 2022-01-01
 */
@Mapper
public interface ApiSchemaRegistryBeanConverter {

    ApiSchemaRegistryBeanConverter INSTANCE = Mappers.getMapper(ApiSchemaRegistryBeanConverter.class);

    SchemaRegistry apiToModel(ApiSchemaRegistry apiSchemaRegistry);

    ApiSchemaRegistry modelToApi(SchemaRegistry schemaRegistry);

    default SchemaRegistry apiToSchemaRegistry(ApiSchemaRegistry apiSchemaRegistry) {
        SchemaRegistry schemaRegistry = apiToModel(apiSchemaRegistry);
        schemaRegistry.setRegistryType(SchemaRegistryType.API);
        //schemaRegistry.setConnectType(ConnectType.API);
        if (StringUtils.isNotEmpty(apiSchemaRegistry.getBodyTemplate())) {
            schemaRegistry.addConfig(HTTP_BODY_TEMPLATE, apiSchemaRegistry.getBodyTemplate());
        }
        if (StringUtils.isNotEmpty(apiSchemaRegistry.getPostScript())) {
            schemaRegistry.addScript(HTTP_POST_SCRIPT, apiSchemaRegistry.getPostScript());
        }
        if (StringUtils.isNotEmpty(apiSchemaRegistry.getPrevScript())) {
            schemaRegistry.addScript(HTTP_PREV_SCRIPT, apiSchemaRegistry.getPrevScript());
        }
        schemaRegistry.addConfig(HTTP_API_URL, apiSchemaRegistry.getAddress());
        schemaRegistry.addConfig(HTTP_METHOD, apiSchemaRegistry.getApiMethod());
        return schemaRegistry;
    }

    default ApiSchemaRegistry schemaRegistryToApi(SchemaRegistry schemaRegistry) {
        ApiSchemaRegistry apiSchemaRegistry = modelToApi(schemaRegistry);

        apiSchemaRegistry.setBodyTemplate(bodyTemplate(schemaRegistry.getConfigs()));
        apiSchemaRegistry.setPostScript(postScript(schemaRegistry.getScript()));
        apiSchemaRegistry.setPrevScript(preScript(schemaRegistry.getScript()));
        apiSchemaRegistry.setApiMethod(httpMethod(schemaRegistry.getConfigs()));
        apiSchemaRegistry.setAddress(url(schemaRegistry.getConfigs()));


        return apiSchemaRegistry;
    }

    default List<ApiSchemaRegistry> schemaRegistriesToApis(List<SchemaRegistry> schemaRegistries) {
        List<ApiSchemaRegistry> apiSchemaRegistries = new ArrayList<>();
        for (SchemaRegistry registry : schemaRegistries) {
            apiSchemaRegistries.add(this.schemaRegistryToApi(registry));
        }
        return apiSchemaRegistries;
    }



    static String bodyTemplate(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_BODY_TEMPLATE));
    }

    static String preScript(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_PREV_SCRIPT));
    }

    static String postScript(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_POST_SCRIPT));
    }

    static ApiMethod httpMethod(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return ApiMethod.valueOf(String.valueOf(configs.get(HTTP_METHOD)));
    }

    static String url(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_API_URL));
    }


}