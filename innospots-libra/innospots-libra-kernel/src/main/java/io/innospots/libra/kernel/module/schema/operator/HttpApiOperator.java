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

package io.innospots.libra.kernel.module.schema.operator;

import io.innospots.base.connector.schema.ApiSchemaRegistry;
import io.innospots.base.connector.schema.ApiSchemaRegistryBeanConverter;
import io.innospots.base.connector.schema.SchemaRegistry;
import io.innospots.base.connector.schema.SchemaRegistryType;
import io.innospots.base.exception.ResourceException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Alfred
 * @date 2021-08-21
 */
@Deprecated
public class HttpApiOperator {

    private final SchemaRegistryOperator schemaRegistryOperator;

    public HttpApiOperator(SchemaRegistryOperator schemaRegistryOperator) {
        this.schemaRegistryOperator = schemaRegistryOperator;
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiSchemaRegistry createApiRegistry(ApiSchemaRegistry apiSchemaRegistry) {
        if (schemaRegistryOperator.checkNameExist(apiSchemaRegistry.getName(), apiSchemaRegistry.getCredentialKey())) {
            throw ResourceException.buildExistException(this.getClass(), apiSchemaRegistry.getName());
        }

        SchemaRegistry schemaRegistry = ApiSchemaRegistryBeanConverter.INSTANCE.apiToSchemaRegistry(apiSchemaRegistry);
        // Set default credentialId
        schemaRegistry.setCredentialKey("");
        schemaRegistry = schemaRegistryOperator.createSchemaRegistry(schemaRegistry);
        return ApiSchemaRegistryBeanConverter.INSTANCE.schemaRegistryToApi(schemaRegistry);
    }

    @Transactional(rollbackFor = Exception.class)
    public ApiSchemaRegistry updateApiRegistry(ApiSchemaRegistry apiSchemaRegistry) {
        if (schemaRegistryOperator.checkNameExist(apiSchemaRegistry.getName(), apiSchemaRegistry.getCredentialKey())) {
            throw ResourceException.buildExistException(this.getClass(), apiSchemaRegistry.getName());
        }
        SchemaRegistry schemaRegistry = ApiSchemaRegistryBeanConverter.INSTANCE.apiToSchemaRegistry(apiSchemaRegistry);
        schemaRegistry = schemaRegistryOperator.updateSchemaRegistry(schemaRegistry);
        return ApiSchemaRegistryBeanConverter.INSTANCE.schemaRegistryToApi(schemaRegistry);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteApiRegistry(Integer schemaRegistryId) {
        return schemaRegistryOperator.deleteSchemaRegistry(schemaRegistryId);
    }

    public ApiSchemaRegistry getApiRegistry(Integer schemaRegistryId) {
        SchemaRegistry schemaRegistry = schemaRegistryOperator.getSchemaRegistryById(schemaRegistryId, true);
        if (schemaRegistry == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "http api registry not exist", schemaRegistryId);
        }
        return ApiSchemaRegistryBeanConverter.INSTANCE.schemaRegistryToApi(schemaRegistry);
    }

    public List<ApiSchemaRegistry> listApiRegistries(String queryCode, String sort) {
        List<SchemaRegistry> schemaRegistries = schemaRegistryOperator.listSchemaRegistries(queryCode, sort, null, SchemaRegistryType.API);
        return ApiSchemaRegistryBeanConverter.INSTANCE.schemaRegistriesToApis(schemaRegistries);
    }

}
