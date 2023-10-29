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

package io.innospots.libra.kernel.module.schema.endpoint;

import io.innospots.base.connector.schema.SchemaRegistry;
import io.innospots.base.connector.schema.reader.CachedSchemaRegistryReader;
import io.innospots.base.connector.schema.reader.ISchemaRegistryReader;
import io.innospots.base.constant.ServiceConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 元数据schema的接口端点
 *
 * @author Smars
 * @date 2021/2/14
 */
@RestController
public class SchemaRegistryReaderEndpoint implements ISchemaRegistryReader {

    private final CachedSchemaRegistryReader dataSchemaReader;


    public SchemaRegistryReaderEndpoint(CachedSchemaRegistryReader dataSchemaReader) {
        this.dataSchemaReader = dataSchemaReader;
    }

    @Override
    @GetMapping(ServiceConstant.PATH_ROOT_DATA_API + "/{credentialId}/schema/registry/list")
    public List<SchemaRegistry> listSchemaRegistries(@PathVariable("credentialId") Integer credentialId,
                                                     @RequestParam(value = "includeField", required = false) boolean includeField) {
        return this.dataSchemaReader.listSchemaRegistries(credentialId, includeField);
    }

    @Override
    @GetMapping(ServiceConstant.PATH_ROOT_DATA_API + "/{credentialId}/schema/registry")
    public SchemaRegistry getSchemaRegistry(@PathVariable("credentialId") Integer credentialId,
                                            @RequestParam(value = "tableName", required = false) String registryCode,
                                            @RequestParam(value = "registryId", required = false) Integer registryId) {
        return dataSchemaReader.getSchemaRegistry(credentialId, registryCode, registryId);
    }

}
