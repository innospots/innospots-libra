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

package io.innospots.base.data.minder;


import io.innospots.base.data.credential.ConnectionCredential;
import io.innospots.base.data.schema.SchemaCatalog;
import io.innospots.base.data.schema.SchemaField;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.data.schema.reader.ISchemaRegistryReader;
import io.innospots.base.utils.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/9/14
 */
public abstract class BaseDataConnectionMinder<Operator> implements IDataConnectionMinder<Operator> {

    protected ISchemaRegistryReader schemaRegistryReader;

    protected ConnectionCredential connectionCredential;

    @Override
    public void initialize(ISchemaRegistryReader schemaRegistryReader, ConnectionCredential connectionCredential) {
        this.schemaRegistryReader = schemaRegistryReader;
        this.connectionCredential = connectionCredential;
    }

    @Override
    public ConnectionCredential connectionCredential() {
        return connectionCredential;
    }

    @Override
    public SchemaRegistry schemaRegistry(String registryCode) {
        return schemaRegistryReader.getSchemaRegistry(connectionCredential.getCredentialKey(), registryCode);
    }

    @Override
    public SchemaRegistry schemaRegistry(Integer registryId) {
        return schemaRegistryReader.getSchemaRegistry(connectionCredential.getCredentialKey(), registryId);
    }

    @Override
    public List<SchemaCatalog> schemaCatalogs() {
        List<SchemaCatalog> schemaCatalogs = new ArrayList<>();
        List<SchemaRegistry> schemaRegistries = schemaRegistries(false);
        for (SchemaRegistry schemaRegistry : schemaRegistries) {
            SchemaCatalog schemaCatalog = BeanUtils.copyProperties(schemaRegistry, SchemaCatalog.class);
            schemaCatalogs.add(schemaCatalog);
        }
        return schemaCatalogs;
    }

    @Override
    public List<SchemaRegistry> schemaRegistries(boolean includeField) {
        return schemaRegistryReader.listSchemaRegistries(connectionCredential.getCredentialKey(), includeField);
    }

}
