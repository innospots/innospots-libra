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

import io.innospots.base.condition.Opt;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.jdbc.SelectClause;
import io.innospots.base.model.Pair;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * sharding table data using sharding sql, split multi sub range sql  eg, select column from table where xxx
 * split number
 *
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/9
 */
@Slf4j
public class DbRangeShardingJob extends AbstractShardingJob<Pair<String, String>> {


    protected IDataOperator dataOperator;

    public DbRangeShardingJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public JobType jobType() {
        return JobType.GROUP;
    }


    @Override
    public void prepare() {
        super.prepare();
        shardingPageSize = validParamInteger(PARAM_SHARDING_PAGE_SIZE);
        jobExecution.setSubJobCount((long) shardingList.size());
    }

    @Override
    protected void fillParams(Map<String, Object> params, Pair<String, String> shardKey) {
        SelectClause selectClause = buildWhereClause(shardKey.getLeft(),shardKey.getRight());
        params.put(PARAM_EXECUTE_READ_CLAUSES, selectClause.buildSql());
    }


    /**
     * select clause
     *
     * @return
     */
    @Override
    protected List<Pair<String, String>> shardingList() {
        List<Pair<String, String>> shardingList = new ArrayList<>();
        String maxId = null;
        String minId;
        DataBody<Map<String, Object>> dataBody = null;
        //TODO Optimized for concurrent splitting using multiple threads
        do {
            SelectClause selectClause = buildRangeClause(table, shardingColumn, shardingPageSize, 1, shardingClause, maxId);
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

    /**
     * select max(s.`primary_id`) as maxId, min(s.`primary_id`) as minId
     * from (select sje.`primary_id` from `xxx` as sje
     * where 1=1 and sje.`primary_id` > '1771806156696584192' order by sje.`primary_id` asc limit 100) as s
     * @param table
     * @param shardingColumn
     * @param shardingSize
     * @param page
     * @param shardingClause
     * @param to
     * @return
     */
    private SelectClause buildRangeClause(String table, String shardingColumn, Integer shardingSize, int page, String shardingClause, String to) {
        SelectClause tableClause = buildTableClause(table, shardingColumn, shardingSize, page, shardingClause, to);
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(" (" + tableClause.buildSql() + ") ");
        selectClause.addColumn("max(" + shardingColumn + ") as maxId");
        selectClause.addColumn("min(" + shardingColumn + ") as minId");
        return selectClause;
    }

    private SelectClause buildWhereClause(String from, String to) {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(table);
        selectClause.addOrderBy(shardingColumn);
        if(from != null){
            selectClause.addWhere(shardingColumn, from, Opt.GREATER_EQUAL);
        }
        if (to != null) {
            selectClause.addWhere(shardingColumn, to, Opt.LESS);
        }
        if (shardingClause != null) {
            selectClause.setWhereClause(shardingClause);
        }

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
