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

package io.innospots.base.data.body;

import io.innospots.base.connector.schema.model.SchemaColumn;

import java.util.List;

/**
 * @author Alfred
 * @date 2022/12/4
 */
public class SqlDataPageBody<T> extends PageBody<T> {

    private List<SchemaColumn> schemaColumns;

    private String tableName;

    public List<SchemaColumn> getSchemaColumns() {
        return this.schemaColumns;
    }

    public void setSchemaColumns(List<SchemaColumn> schemaColumns) {
        this.schemaColumns = schemaColumns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
