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

package io.innospots.workflow.node.app.connector;

import com.google.common.base.Enums;
import io.innospots.base.condition.Factor;
import io.innospots.base.data.enums.DataOperation;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.data.operator.jdbc.UpdateItem;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.data.request.SimpleRequest;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/3/16
 */
public class SqlDataNode extends DataNode {

    private static final Logger logger = LoggerFactory.getLogger(SqlDataNode.class);

    public static final String FIELD_SQL_CLAUSE = "sql_clause";
    public static final String FIELD_QUERY_CONDITION = "query_conditions";
    public static final String FIELD_UPDATE_CONDITION = "update_conditions";

    public static final String FIELD_COLUMN_MAPPING = "column_mapping";

    public static final String FIELD_DB_NAME = "db_name";

    public static final String FIELD_TABLE_NAME = "table_name";
    public static final String FIELD_OPERATION = "data_operation";
    public static final String KEY_COLUMN = "key_column";
    public static final String COLUMN_UPDATE_TIME = "updated_time";

    /**
     * sql中参数的开始和结束字符
     */
    private static final String SQL_PARAM_START = "${";
    private static final String SQL_PARAM_END = "}";

    protected DataOperation operation;

    /**
     * the columns when insert or update operation
     */
    protected List<Factor> columnFields;

    protected String tableName;

    protected String keyColumn;
    protected String updateTimeColumn;

    /**
     * where clause, when update operation
     */
    protected List<Factor> queryConditions;

    protected List<Factor> updateConditions;

    private String sqlQueryClause;

    @Override
    protected void initialize() {
        super.initialize();
        operation = Enums.getIfPresent(DataOperation.class, ni.containsKey(FIELD_OPERATION) ? ni.valueString(FIELD_OPERATION) : "").orNull();
        tableName = valueString(FIELD_TABLE_NAME);

        //solve enum key not exits or key is null
        List<Map<String, Object>> columnFieldMapping = valueMapList(FIELD_COLUMN_MAPPING);
        if ((operation == DataOperation.INSERT || operation == DataOperation.UPSERT) && columnFieldMapping == null) {
            throw ConfigException.buildMissingException(this.getClass(), this.nodeKey(), FIELD_COLUMN_MAPPING);
        }
        if (operation == DataOperation.UPDATE && columnFieldMapping == null) {
            throw ConfigException.buildMissingException(this.getClass(), this.nodeKey(), FIELD_COLUMN_MAPPING);
        }
        columnFields = BeanUtils.toBean(columnFieldMapping, Factor.class);
        if (DataOperation.UPDATE == operation) {
            columnFields = columnFields.stream().filter(f -> !f.checkNull()).collect(Collectors.toList());
        }

        keyColumn = valueString(KEY_COLUMN);

        if (DataOperation.UPSERT == operation) {
            IDataConnectionMinder connectionMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
            if (connectionMinder != null) {
                List<SchemaField> schemaFields = connectionMinder.schemaRegistryByCode(tableName).getSchemaFields();
                schemaFields = schemaFields.stream().filter(SchemaField::getPkey).collect(Collectors.toList());
                keyColumn = "";
                for (int i = 0; i < schemaFields.size(); i++) {
                    keyColumn += schemaFields.get(i).getCode();
                    if (i < schemaFields.size() - 1) {
                        keyColumn += ",";
                    }
                }//end for
            }
        }//end upsert

        logger.info("table name:{}, primary name:{}",tableName,keyColumn);

        updateTimeColumn = valueString(COLUMN_UPDATE_TIME);

        List<Map<String, Object>> updateConditionFields = valueMapList(FIELD_UPDATE_CONDITION);
        if (operation == DataOperation.UPDATE) {
            if (updateConditionFields == null) {
                throw ConfigException.buildMissingException(this.getClass(), this.nodeKey(), FIELD_UPDATE_CONDITION);
            }
            updateConditions = BeanUtils.toBean(updateConditionFields, Factor.class);
        }

        List<Map<String, Object>> queryConditionFields = valueMapList(FIELD_QUERY_CONDITION);

        if (queryConditionFields != null) {
            queryConditions = BeanUtils.toBean(queryConditionFields, Factor.class);
        }


        sqlQueryClause = valueString(FIELD_SQL_CLAUSE);
        if (sqlQueryClause != null) {
            sqlQueryClause = sqlQueryClause.replaceAll("\\n", " ");
            if (operation == null) {
                operation = DataOperation.LIST;
            }
        }
        if (operation == DataOperation.LIST || operation == DataOperation.GET) {
            fillOutputConfig();
        }
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        switch (operation) {
            case GET:
                fetchOne(nodeExecution);
                break;
            case LIST:
                query(nodeExecution);
                break;
            case INSERT:
                insert(nodeExecution);
                break;
            case UPDATE:
                update(nodeExecution);
                break;
            case UPSERT:
                upsert(nodeExecution);
                break;
            default:
                logger.warn("data operation not set correctly:{} , execution:{}", operation, nodeExecution);
                break;
        }
    }


    private String parseSqlParam(Map<String, Object> item) {
        return parseSqlParam(sqlQueryClause, item, true);
    }


    private String parseSqlParam(String sql, Map<String, Object> item, boolean checkParam) {
        //String sql = sqlQueryClause;

        if (CollectionUtils.isNotEmpty(this.queryConditions)) {
            for (Factor conditionField : this.queryConditions) {
                if (sql.contains(conditionField.getName())) {
                    Object value = conditionField.value(item);
                    if (value == null) {
                        throw ConfigException.buildMissingException(this.getClass(), "sql param:" + conditionField.getCode() + " value is null");
                    }
                    String nm = conditionField.getName();
                    nm = nm.replace("${","\\$\\{").replace("}","\\}");
                    sql = sql.replaceAll(nm, value.toString());
//                    sql = sql.replaceAll("\\$\\{" + conditionField.getCode() + "\\}", value.toString());
                }
            }
        }


        int noReplaceParamIdx = sql.indexOf(SQL_PARAM_START);
        if (checkParam && noReplaceParamIdx > 0) {
            String paramName = sql.substring(noReplaceParamIdx + 2, sql.lastIndexOf(SQL_PARAM_END));
            throw ConfigException.buildMissingException(this.getClass(), "sql param:" + paramName + " not replace");
        }
        logger.debug("parse sql:{}", sql);
        return sql;
    }


    protected void insert(NodeExecution nodeExecution) {
        List<Map<String, Object>> insertList = new ArrayList<>();
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                Map<String, Object> insertData = new HashMap<>();
                for (Factor columnField : this.columnFields) {
                    insertData.put(columnField.getCode(), columnField.value(item));
                }
                if (this.updateTimeColumn != null && !insertData.containsKey(this.updateTimeColumn)) {
                    insertData.put(this.updateTimeColumn, LocalDateTime.now());
                }
                insertList.add(insertData);
                fillOutput(nodeOutput, item);
            }// end for item
        }//end for input
        //TODO
//        InnospotResponse<Integer> resp = dataOperatorPoint.insertBatch(credentialId, tableName, insertList);

//        nodeExecution.setMessage(resp.getMessage());
    }


    protected void update(NodeExecution nodeExecution) {
        List<UpdateItem> updateItems = new ArrayList<>();
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                Map<String, Object> upData = new HashMap<>();
                for (Factor columnField : this.columnFields) {
                    upData.put(columnField.getCode(), columnField.value(item));
                }
                if (this.updateTimeColumn != null && !upData.containsKey(this.updateTimeColumn)) {
                    upData.put(this.updateTimeColumn, LocalDateTime.now());
                }
                List<Factor> conditions = conditionValues(item, this.updateConditions);
                UpdateItem updateItem = new UpdateItem();
                updateItem.setData(upData);
                updateItem.setConditions(conditions);
                updateItems.add(updateItem);
                fillOutput(nodeOutput, item);
            }//end for item
        }//end for execution input
//        InnospotResponse<Integer> resp = dataOperatorPoint.updateForBatch(credentialId, tableName, updateItems);
//        nodeExecution.setMessage(resp.getMessage());
    }


    protected void fetchOne(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                for (Map<String, Object> item : executionInput.getData()) {
                    String sql = parseSqlParam(item);

                    DataBody<Map<String, Object>> dataBody = dataOperator.execute(buildRequest(sql));
                    if (logger.isDebugEnabled()) {
                        logger.debug("sql query:{}, response:{}", sql, dataBody);
                    }
                    Object data = dataBody.getBody();
                    fillOutput(nodeOutput, item, data);
                }//end item
            }//end execution input
        } else {
            String sql = parseSqlParam(null);

            DataBody<Map<String, Object>> dataBody = dataOperator.execute(buildRequest(sql));
            if (logger.isDebugEnabled()) {
                logger.debug("sql query:{}, response:{}", sql, dataBody);
            }
            Object data = dataBody.getBody();
            fillOutput(nodeOutput, null, data);
        }
    }


    protected void query(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        if (CollectionUtils.isNotEmpty(nodeExecution.getInputs()) &&
                CollectionUtils.isNotEmpty(nodeExecution.getInputs().get(0).getData())) {
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                for (Map<String, Object> item : executionInput.getData()) {
                    String sql = parseSqlParam(item);
                    PageBody body = dataOperator.executePage(buildRequest(sql));
                    if (logger.isDebugEnabled()) {
                        logger.debug("sql query:{}, response:{}", sql, body);
                    }
                    fillOutput(nodeOutput, item, body.getList());
                }//end item
            }//end execution input
        } else {
            String sql = parseSqlParam(null);
            PageBody pageBody = dataOperator.executePage(buildRequest(sql));
            if (logger.isDebugEnabled()) {
                logger.debug("sql query:{}, response:{}", sql, pageBody);
            }
            fillOutput(nodeOutput, null, pageBody.getList());
        }
    }

    protected void upsert(NodeExecution nodeExecution) {
        List<Map<String, Object>> insertList = new ArrayList<>();
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                Map<String, Object> insertData = new HashMap<>();
                for (Factor columnField : this.columnFields) {
                    insertData.put(columnField.getCode(), columnField.value(item));
                }
                if (this.updateTimeColumn != null && !insertData.containsKey(this.updateTimeColumn)) {
                    insertData.put(this.updateTimeColumn, LocalDateTime.now());
                }
                insertList.add(insertData);
                fillOutput(nodeOutput, item);
            }// end for item
        }//end for input
        int resp = dataOperator.upsertBatch(tableName, keyColumn, insertList);

        nodeExecution.setMessage("");
    }

    private SimpleRequest buildRequest(String sql) {
        SimpleRequest simpleRequest = new SimpleRequest(sql);
        simpleRequest.setOperation(this.operation.name());

        return simpleRequest;
    }


}
