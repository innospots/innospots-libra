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

import io.innospots.base.condition.Opt;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.jdbc.SelectClause;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.Pair;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * split table data using split script,eg, select column from table where xxx
 * split number
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/9
 */
@Slf4j
public class DbShardingJob extends BaseJob {


    public static final String PARAM_SHARDING_COLUMN = "job.sharding.column";

    public static final String PARAM_SHARDING_CLAUSE = "job.sharding.clause";

    public static final String PARAM_SHARDING_TABLE = "job.sharding.table";

    public static final String PARAM_SHARDING_SIZE = "job.sharding.size";

    public static final String PARAM_CREDENTIAL_KEY = "job.credential.key";

    public static final String PARAM_EXECUTE_JOB_KEY = "job.execute.job_key";

    public static final String PARAM_EXECUTE_JOB_PARAMS = "job.execute.job_params";

    public static final String PARAM_RANGE_FROM = "job.range.from";

    public static final String PARAM_RANGE_TO = "job.range.to";

    protected String credentialKey;

    protected String table;

    protected String shardingColumn;

    protected Integer shardingSize;

    protected String shardingClause;

    protected IDataOperator dataOperator;

    protected String executeJobKey;

    protected List<Pair<String, String>> shardingList;

    protected ReadJobDispatcher readJobDispatcher;

    @Override
    public JobType jobType() {
        return JobType.GROUP;
    }


    @Override
    public void prepare() {
        credentialKey = validParamString(PARAM_CREDENTIAL_KEY);
        table = validParamString(PARAM_SHARDING_TABLE);
        IDataConnectionMinder minder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        shardingColumn = shardingColumn(minder);
        shardingSize = validParamInteger(PARAM_SHARDING_SIZE);
        shardingClause = jobExecution.getString(PARAM_SHARDING_CLAUSE);
        dataOperator = minder.buildOperator();
        executeJobKey = validParamString(PARAM_EXECUTE_JOB_KEY);

        //sharding sub execute job key
        List<Pair<String, String>> shardingList = shardingList();
        jobExecution.setSubJobCount((long) shardingList.size());
        readJobDispatcher = BeanContextAwareUtils.getBean(ReadJobDispatcher.class);
    }

    protected String shardingColumn(IDataConnectionMinder minder) {
        return validParamString(PARAM_SHARDING_COLUMN);
    }

    @Override
    public void execute() {
        if (shardingList.isEmpty()) {
            log.warn("job sharding is empty,jobKey:{}, executionId:{}, credentialKey:{}, table:{}", jobExecution.getJobKey(), jobExecution.getExecutionId(), credentialKey, table);
            return;
        }
        dispatchSubShardingJob(shardingList);
        log.info("job sharding count:{}, jobKey:{}, jobExecutionId:{}", shardingList.size(), jobExecution.getJobKey(), jobExecution.getExecutionId());
    }

    /**
     * push sharding sub job to ready queue using dispatcher
     *
     * @param shardingList
     */
    private void dispatchSubShardingJob(List<Pair<String, String>> shardingList) {
        //execute job params that define in the parent sharding job
        Map prm = getParamMap(PARAM_EXECUTE_JOB_PARAMS);
        int seq = 0;
        for (Pair<String, String> pair : shardingList) {
            seq++;
            Map<String, Object> params = new HashMap<>();
            String fromValue = pair.getLeft();
            String toValue = pair.getRight();
            params.put(PARAM_RANGE_FROM, fromValue);
            params.put(PARAM_RANGE_TO, toValue);
            if (prm != null) {
                params.putAll(prm);
            }
            log.info("dispatch jobKey:{}, params:{}", executeJobKey, params);
            readJobDispatcher.execute(jobExecution.getExecutionId(), seq, executeJobKey, params);
        }//end for
    }


    /**
     * select clause
     *
     * @return
     */
    private List<Pair<String, String>> shardingList() {
        List<Pair<String, String>> shardingList = new ArrayList<>();
        String maxId = null;
        String minId;
        DataBody<Map<String, Object>> dataBody = null;
        do {
            SelectClause selectClause = buildRangeClause(table, shardingColumn, shardingSize, 1, shardingClause, maxId);
            dataOperator.selectForObject(selectClause);
            dataBody = dataOperator.selectForObject(selectClause);
            Object v = dataBody.getBody().get("maxId");
            maxId = v != null ? v.toString() : null;
            v = dataBody.getBody().get("minId");
            minId = v != null ? v.toString() : null;
            log.info("sharding clause:{}, maxId:{}, minId:{}", selectClause.buildSql(), maxId, minId);
            Pair<String, String> pair = Pair.of(minId, maxId);
            shardingList.add(pair);
        } while (maxId != null);

        return shardingList;
    }

    private SelectClause buildRangeClause(String table, String shardingColumn, Integer shardingSize, int page, String shardingClause, String to) {
        SelectClause tableClause = buildTableClause(table, shardingColumn, shardingSize, page, shardingClause, to);
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(" (" + tableClause.buildSql() + ") ");
        selectClause.addColumn("max(" + shardingColumn + ") as maxId");
        selectClause.addColumn("min(" + shardingColumn + ") as minId");
        return selectClause;
    }


    private SelectClause buildTableClause(String table, String shardingColumn, Integer shardingSize, int page, String shardingClause, String to) {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(table);
        selectClause.setPage(page);
        selectClause.setSize(shardingSize);
        selectClause.addOrderBy(shardingColumn);
        selectClause.addColumn(shardingColumn);
        if (to != null) {
            selectClause.addWhere(shardingColumn, to, Opt.GREATER);
        }
        if (shardingClause != null) {
            selectClause.setWhereClause(shardingClause);
        }

        return selectClause;
    }
}
