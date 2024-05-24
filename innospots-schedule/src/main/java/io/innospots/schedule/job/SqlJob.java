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

package io.innospots.schedule.job;

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.request.SimpleRequest;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/24
 */
@Slf4j
public class SqlJob extends BaseJob {

    public static String PARAM_SCRIPT_BODY = "job.script.body";
    public static final String PARAM_CREDENTIAL_KEY = "job.credential.key";

    private String sqlScript;
    private String credentialKey;
    private IOperator operator;

    public SqlJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public void prepare() {
        credentialKey = validParamString(PARAM_CREDENTIAL_KEY);
        sqlScript = validParamString(PARAM_SCRIPT_BODY);
        IDataConnectionMinder minder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        operator = minder.buildOperator();
    }

    @Override
    public InnospotResponse<Map<String,Object>> execute() {
        InnospotResponse<Map<String,Object>> response = new InnospotResponse<>();
        SimpleRequest simpleRequest = new SimpleRequest(credentialKey, sqlScript);
        log.info("execute sql job, credentialKey:{}, sqlScript:{}", credentialKey, sqlScript);
        DataBody dataBody = operator.execute(simpleRequest);
        log.info("sql execute job:{}", dataBody);
        return response;
    }
}
