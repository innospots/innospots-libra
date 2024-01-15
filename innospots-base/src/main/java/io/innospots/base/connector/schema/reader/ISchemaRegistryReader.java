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

package io.innospots.base.connector.schema.reader;

import io.innospots.base.connector.schema.model.SchemaRegistry;

import java.util.List;

/**
 * data source schema reader
 *
 * @author Raydian
 * @date 2021/2/5
 */
public interface ISchemaRegistryReader {


    List<SchemaRegistry> listSchemaRegistries(String credentialKey, boolean includeField);

    SchemaRegistry getSchemaRegistryByCode(String credentialKey, String registryCode);

    SchemaRegistry getSchemaRegistryById(String credentialKey, String registryId);
}
