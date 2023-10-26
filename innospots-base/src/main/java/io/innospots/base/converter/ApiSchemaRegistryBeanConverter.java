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

package io.innospots.base.converter;

import io.innospots.base.data.http.HttpDataExecutor;
import io.innospots.base.data.schema.ApiSchemaRegistry;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.data.schema.SchemaRegistryType;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Alfred
 * @date 2022-01-01
 */
@Deprecated
@Mapper
public interface ApiSchemaRegistryBeanConverter extends BaseBeanConverter {

    ApiSchemaRegistryBeanConverter INSTANCE = Mappers.getMapper(ApiSchemaRegistryBeanConverter.class);

    SchemaRegistry apiToModel(ApiSchemaRegistry apiSchemaRegistry);

    ApiSchemaRegistry modelToApi(SchemaRegistry schemaRegistry);

    default SchemaRegistry apiToSchemaRegistry(ApiSchemaRegistry apiSchemaRegistry) {
        SchemaRegistry schemaRegistry = apiToModel(apiSchemaRegistry);
        schemaRegistry.setRegistryType(SchemaRegistryType.API);
        //schemaRegistry.setConnectType(ConnectType.API);
        if (StringUtils.isNotEmpty(apiSchemaRegistry.getBodyTemplate())) {
            schemaRegistry.addConfig(HttpDataExecutor.HTTP_BODY_TEMPLATE, apiSchemaRegistry.getBodyTemplate());
        }
        if (StringUtils.isNotEmpty(apiSchemaRegistry.getPostScript())) {
            schemaRegistry.addScript(HttpDataExecutor.HTTP_POST_SCRIPT, apiSchemaRegistry.getPostScript());
        }
        if (StringUtils.isNotEmpty(apiSchemaRegistry.getPrevScript())) {
            schemaRegistry.addScript(HttpDataExecutor.HTTP_PREV_SCRIPT, apiSchemaRegistry.getPrevScript());
        }
        schemaRegistry.addConfig(HttpDataExecutor.HTTP_API_URL, apiSchemaRegistry.getAddress());
        schemaRegistry.addConfig(HttpDataExecutor.HTTP_METHOD, apiSchemaRegistry.getApiMethod());
        return schemaRegistry;
    }

    default ApiSchemaRegistry schemaRegistryToApi(SchemaRegistry schemaRegistry) {
        ApiSchemaRegistry apiSchemaRegistry = modelToApi(schemaRegistry);

        apiSchemaRegistry.setBodyTemplate(HttpDataExecutor.bodyTemplate(schemaRegistry.getConfigs()));
        apiSchemaRegistry.setPostScript(HttpDataExecutor.postScript(schemaRegistry.getScript()));
        apiSchemaRegistry.setPrevScript(HttpDataExecutor.preScript(schemaRegistry.getScript()));
        apiSchemaRegistry.setApiMethod(HttpDataExecutor.httpMethod(schemaRegistry.getConfigs()));
        apiSchemaRegistry.setAddress(HttpDataExecutor.url(schemaRegistry.getConfigs()));


        return apiSchemaRegistry;
    }

    default List<ApiSchemaRegistry> schemaRegistriesToApis(List<SchemaRegistry> schemaRegistries) {
        List<ApiSchemaRegistry> apiSchemaRegistries = new ArrayList<>();
        for (SchemaRegistry registry : schemaRegistries) {
            apiSchemaRegistries.add(this.schemaRegistryToApi(registry));
        }
        return apiSchemaRegistries;
    }

}