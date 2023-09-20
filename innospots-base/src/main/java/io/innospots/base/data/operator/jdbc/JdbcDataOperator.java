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

package io.innospots.base.data.operator.jdbc;

import io.innospots.base.condition.Factor;
import io.innospots.base.condition.Mode;
import io.innospots.base.condition.Opt;
import io.innospots.base.condition.statement.FactorStatementBuilder;
import io.innospots.base.condition.statement.IFactorStatement;
import io.innospots.base.data.enums.DataOperation;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.SelectClause;
import io.innospots.base.data.operator.UpdateItem;
import io.innospots.base.exception.data.DataOperationException;
import io.innospots.base.model.DataBody;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.field.FieldValueType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Smars
 * @date 2021/5/5
 */
@Slf4j
public class JdbcDataOperator implements IDataOperator {

    protected JdbcTemplate jdbcTemplate;

    protected int max_batch_size = 200;

    protected IFactorStatement factorStatement = FactorStatementBuilder.build(Mode.DB);

    public JdbcDataOperator(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public PageBody<Map<String, Object>> selectForList(String tableName, List<Factor> condition, int page, int size) {
        PageBody pageBody = new PageBody();

        SQL countSql = new SQL().SELECT("COUNT(1)").FROM(tableName);
        Long count = jdbcTemplate.queryForObject(countSql.toString(), Long.class);
        pageBody.setTotalPage(count);

        String limit = (page - 1) * size + "," + size;
        SQL sql = new SQL().SELECT("*").FROM(tableName).LIMIT(limit);
        for (Factor factor : condition) {
            // TODO 缺少OR AND 条件
            sql.WHERE(factorStatement.statement(factor));
        }

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString());
//        pageBody.setBody(mapKeyToCamel(result));
        pageBody.setList(result);
        pageBody.setCurrent((long) page);
        pageBody.setPageSize((long) size);
        return pageBody;
    }

    @Override
    public PageBody<Map<String, Object>> selectForList(SelectClause selectClause) {
        PageBody<Map<String, Object>> pageBody = new PageBody<>();
        String sql = selectClause.buildSql();
        log.debug("select sql:{}", sql);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        if (CollectionUtils.isNotEmpty(result)) {
            pageBody.setList(result);
            //pageBody.setBody(mapKeyToCamel(result));
            pageBody.setCurrent(1L);
            pageBody.setPageSize((long) result.size());
        }

        return pageBody;
    }

    @Override
    public PageBody<Map<String, Object>> selectLatest(String tableName, String upTimeField, int size) {
        //TODO
        return null;
    }

    @Override
    public DataBody<Map<String, Object>> selectForObject(String tableName, List<Factor> condition) {
        SQL sql = new SQL().SELECT("*").FROM(tableName);
        for (Factor factor : condition) {
            // TODO 缺少OR AND 条件
            sql.WHERE(factorStatement.statement(factor));
        }

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString());
        DataBody<Map<String, Object>> dataBody = new DataBody<>();
        if (CollectionUtils.isNotEmpty(result)) {
            dataBody.setBody(result.get(0));
        }
        return dataBody;
    }


    @Override
    public DataBody<Map<String, Object>> selectForObject(String tableName, String key, String value) {
        DataBody<Map<String, Object>> dataBody = new DataBody<>();
        SQL sql = new SQL().SELECT("*").FROM(tableName).WHERE(key + Opt.EQUAL.symbol(Mode.DB) + "'" + value + "'");
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString());
        if (CollectionUtils.isNotEmpty(result)) {
            dataBody.setBody(result.get(0));
        }
        dataBody.setConsume(Math.toIntExact(System.currentTimeMillis() - dataBody.getStartTime()));
        return dataBody;
    }

    @Override
    public DataBody<Map<String, Object>> selectForObject(SelectClause selectClause) {
        long start = System.currentTimeMillis();
        log.info("执行的sql: {}", selectClause.buildSql());
        Map<String, Object> result = jdbcTemplate.queryForMap(selectClause.buildSql());
        DataBody<Map<String, Object>> dataBody = new DataBody();
        dataBody.setBody(result);
        dataBody.setStartTime(start);
        dataBody.setConsume(Math.toIntExact(System.currentTimeMillis() - start));
        return dataBody;
    }

    @Override
    public Integer insert(String tableName, Map<String, Object> data) {
        SQL sql = new SQL().INSERT_INTO(tableName);

        List<Factor> factors = new ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                Factor factor = new Factor(entry.getKey(), Opt.EQUAL, entry.getValue(), FieldValueType.convertTypeByValue(entry.getValue()));
                factors.add(factor);
            }
        }
        for (Factor factor : factors) {
            sql.INTO_COLUMNS(factor.getCode());
            sql.INTO_VALUES(String.valueOf(factorStatement.normalizeValue(factor)));
            //sql.INTO_VALUES(factor.getCode(), String.valueOf(factorStatement.normalizeValue(factor)));
        }

        int insert;
        try {
            insert = jdbcTemplate.update(sql.toString());
        } catch (Exception e) {
            log.error("jdbc operator insert error:{}", sql.toString(), e);
            throw DataOperationException.buildException(this.getClass(), DataOperation.INSERT, "Data insert fail", e);
        }
        return insert;
    }

    @Override
    public Integer insertBatch(String tableName, List<Map<String, Object>> data) {

        if (CollectionUtils.isEmpty(data)) {
            return 0;
        }

        SQL sql = new SQL().INSERT_INTO(tableName);

        Map<String, Object> map = data.get(0);
        for (String key : map.keySet()) {
            sql.INTO_COLUMNS(key);
        }


        for (Map<String, Object> dataMap : data) {
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String value = String.valueOf(factorStatement.normalizeValue(entry.getValue(), FieldValueType.convertTypeByValue(entry.getValue())));
                sql.INTO_VALUES(value);
            }
            sql.ADD_ROW();
        }

        int[] insertBatch;
        try {
            insertBatch = jdbcTemplate.batchUpdate(sql.toString());
        } catch (Exception e) {
            throw DataOperationException.buildException(this.getClass(), DataOperation.INSERT, "Data insert batch fail", e);
        }
        return insertBatch.length;

    }

    @Override
    public Integer upsert(String tableName, String keyColumn, Map<String, Object> data) {
        if (MapUtils.isEmpty(data)) {
            return 0;
        }

        SQL sql = new SQL().INSERT_INTO(tableName);

        data.forEach((k, v) -> {
            sql.INTO_COLUMNS(k);
            String value = String.valueOf(factorStatement.normalizeValue(v, FieldValueType.convertTypeByValue(v)));
            sql.INTO_VALUES(value);
        });
        sql.ADD_ROW();

        StringBuilder sb = new StringBuilder(sql.toString());
        sb.append(" ON DUPLICATE KEY UPDATE ");
        data.remove(keyColumn);

        int i = 0;
        for (String key : data.keySet()) {
            sb.append(key).append(" = VALUES(").append(key).append(")");
            if (i != data.keySet().size() - 1) {
                sb.append(",");
            }
            i++;
        }

        int upsert = jdbcTemplate.update(sb.toString());
        return upsert;
    }

    @Override
    public Integer upsertBatch(String tableName, String keyColumn, List<Map<String, Object>> data) {
        if(data.size() <= max_batch_size){
            return eachBatchUpsert(tableName,keyColumn,data);
        }
        int count = 0;
        List<Map<String,Object>> items = new ArrayList<>();
        for (Map<String, Object> item : data) {
            if(items.size()< max_batch_size){
                items.add(item);
            }else{
                count += eachBatchUpsert(tableName,keyColumn,items);
                items.clear();
            }
        }//end for
        if(items.size() >0){
            count += eachBatchUpsert(tableName,keyColumn,items);
        }

        return count;
    }


    public Integer eachBatchUpsert(String tableName, String keyColumn, List<Map<String, Object>> data){
        if (CollectionUtils.isEmpty(data)) {
            return 0;
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");
        Map<String, Object> firstData = data.get(0);
        sqlBuilder.append(String.join(", ", firstData.keySet())).append(")");
        sqlBuilder.append(" VALUES (");
        for (int i = 0; i < firstData.size(); i++) {
            if(i < firstData.size() - 1){
                sqlBuilder.append("?, ");
            }else{
                sqlBuilder.append("?");
            }
        }//end for
        sqlBuilder.append(") ON DUPLICATE KEY UPDATE ");

        Set<String> columns = new HashSet<>(firstData.keySet());
        Arrays.stream(keyColumn.split(",")).collect(Collectors.toList()).forEach(columns::remove);

        int i = 0;
        for (String key : columns) {
            sqlBuilder.append(key).append(" = VALUES(").append(key).append(")");
            if (i != columns.size() - 1) {
                sqlBuilder.append(",");
            }
            i++;
        }

        List<Object[]> inData = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Object[] obj = new Object[item.size()];
            int j = 0;
            for (String k : item.keySet()) {
                obj[j++] = item.get(k);
            }
            inData.add(obj);
        }

        if(log.isDebugEnabled()){
            log.debug("upsert size:{}, sql:{}",inData.size(),sqlBuilder);
        }

        int upsert = 0;
        try {
            int[] ups = jdbcTemplate.batchUpdate(sqlBuilder.toString(),inData);
            upsert = Arrays.stream(ups).sum();
        }catch (RuntimeException e){
            log.error(e.getMessage(),e);
            throw e;
        }

        return upsert;
    }

    @Override
    public Integer update(String tableName, UpdateItem item) {
        int update;
        try {
            String sql = item.buildSql(tableName, factorStatement);
            update = jdbcTemplate.update(sql);
        } catch (Exception e) {
            throw DataOperationException.buildException(this.getClass(), DataOperation.UPDATE, "Data update fail", e);
        }
        return update;
    }

    @Override
    public Integer updateForBatch(String tableName, List<UpdateItem> items) {
        String[] sqlList = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            sqlList[i] = items.get(i).buildSql(tableName, factorStatement);
        }

        int[] updateBatch;
        try {
            updateBatch = jdbcTemplate.batchUpdate(sqlList);
        } catch (Exception e) {
            throw DataOperationException.buildException(this.getClass(), DataOperation.UPDATE, "data update batch fail, sql: " + Arrays.toString(sqlList), e);
        }
        return updateBatch.length;
    }

    @Override
    public Integer delete(String tableName, List<Factor> conditions) {
        SQL sql = new SQL().DELETE_FROM(tableName);
        String[] stmtCons = new String[conditions.size()];
        for (int j = 0; j < conditions.size(); j++) {
            stmtCons[j] = factorStatement.statement(conditions.get(j));
        }
        sql.WHERE(stmtCons);

        int delete;
        try {
            delete = jdbcTemplate.update(sql.toString());
        } catch (Exception e) {
            throw DataOperationException.buildException(this.getClass(), DataOperation.DELETE, "Data delete fail", e);
        }
        return delete;
    }

    @Override
    public Integer deleteBatch(String tableName, List<Factor> condition) {
        // TODO 参数与delete一致，待修改
        return null;
    }

}
