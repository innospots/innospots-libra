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

package io.innospots.schedule.job.db.sharding;

import cn.hutool.core.text.StrFormatter;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.schedule.exception.JobExecutionException;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * split job according primary column
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/9
 */
@Slf4j
public class DbPkRangeShardingJob extends DbRangeShardingJob {


    public DbPkRangeShardingJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    protected String shardingColumn() {
        IDataConnectionMinder minder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        SchemaRegistry schemaRegistry = minder.schemaRegistryByCode(table);
        Optional<SchemaField> schemaField = schemaRegistry.getSchemaFields().stream().filter(SchemaField::getPkey).findFirst();
        if (schemaField.isPresent()) {
            return schemaField.get().getCode();
        } else {
            throw new JobExecutionException(this.getClass(), ResponseCode.PARAM_NULL,StrFormatter.format("Table {}'s schema primary key field is empty", table));
        }
    }
}
