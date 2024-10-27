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

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.model.response.R;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The table data is segmented,
 * and the job to be processed is divided into multiple sub-shard jobs
 * according to the specified conditions and the number of shards
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/9
 */
@Slf4j
public abstract class AbstractShardingJob<T> extends BaseJob {


    public static final String PARAM_SHARDING_COLUMN = "job.sharding.column";

    public static final String PARAM_SHARDING_CLAUSE = "job.sharding.clause";

    public static final String PARAM_SHARDING_TABLE = "job.sharding.table";

    public static final String PARAM_SHARDING_COUNT = "job.sharding.count";

    public static final String PARAM_SHARDING_PAGE_SIZE = "job.sharding.page.size";

    public static final String PARAM_CREDENTIAL_KEY = "job.credential.key";

    public static final String PARAM_EXECUTE_JOB_KEY = "job.execute.job_key";

    public static final String PARAM_EXECUTE_READ_CLAUSES = "job.execute.read_clause";

    protected String credentialKey;

    protected String table;

    protected String shardingColumn;

    protected Integer shardingPageSize;

    protected Integer shardingCount;

    protected IDataOperator dataOperator;

    protected String shardingClause;

    protected String executeJobKey;

    protected List<T> shardingList;

    protected ReadJobDispatcher readJobDispatcher;

    public AbstractShardingJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public JobType jobType() {
        return JobType.GROUP;
    }


    @Override
    public void prepare() {
        credentialKey = validParamString(PARAM_CREDENTIAL_KEY);
        table = validParamString(PARAM_SHARDING_TABLE);
        //shardingPageSize = validParamInteger(PARAM_SHARDING_PAGE_SIZE);
        //sharding where clause
        shardingClause = jobExecution.getString(PARAM_SHARDING_CLAUSE);
        executeJobKey = validParamString(PARAM_EXECUTE_JOB_KEY);
        IDataConnectionMinder minder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        dataOperator = minder.buildOperator();
        readJobDispatcher = BeanContextAwareUtils.getBean(ReadJobDispatcher.class);
        shardingColumn = shardingColumn();
        //sharding sub execute job key
        shardingList = shardingList();
    }

    protected String shardingColumn() {
        return validParamString(PARAM_SHARDING_COLUMN);
    }

    @Override
    public R<Map<String, Object>> execute() {
        if (shardingList.isEmpty()) {
            log.warn("job sharding is empty,jobKey:{}, executionId:{}, credentialKey:{}, table:{}", jobExecution.getJobKey(), jobExecution.getExecutionId(), credentialKey, table);
            return null;
        }
        dispatchSubShardingJob();
        log.info("job sharding count:{}, jobKey:{}, jobExecutionId:{}", shardingList.size(), jobExecution.getJobKey(), jobExecution.getExecutionId());
        return null;
    }

    /**
     * push sharding sub job to ready queue using dispatcher
     *
     */
    protected void dispatchSubShardingJob() {
        //execute job params that define in the parent sharding job
        int seq = 0;
        for (T shardKey : shardingList) {
            seq++;
            Map<String, Object> params = buildShardingExecuteJobParam();
            fillParams(params,shardKey);
            log.info("dispatch jobKey:{}, params:{}", executeJobKey, params);
            readJobDispatcher.dispatch(jobExecution.getExecutionId(), seq, executeJobKey, params);
        }//end for
    }

    protected abstract void fillParams(Map<String,Object> params,T shardKey);

    protected Map<String,Object> buildShardingExecuteJobParam(){
        Map prm = getParamMap(PARAM_EXECUTE_JOB_PARAMS);
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_CREDENTIAL_KEY, credentialKey);
        params.put(PARAM_SHARDING_TABLE,table);
        params.put(PARAM_SHARDING_COLUMN,shardingColumn);
        params.put(PARAM_EXECUTE_JOB_KEY,executeJobKey);
        if (prm != null) {
            params.putAll(prm);
        }
        return params;
    }


    /**
     * select clause
     *
     * @return
     */
    protected abstract List<T> shardingList();


}
