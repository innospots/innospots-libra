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

package io.innospots.libra.kernel.module.schema.dataset;

import datart.Column;
import datart.PageInfo;
import datart.ViewExecuteParam;
import datart.base.consts.ValueType;
import datart.base.consts.VariableTypeEnum;
import datart.provider.Dataframe;
import datart.provider.ScriptVariable;
import datart.provider.StdSqlOperator;
import io.innospots.base.connector.credential.ConnectionCredential;
import io.innospots.base.connector.credential.IConnectionCredentialReader;
import io.innospots.base.connector.schema.SchemaColumn;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.body.SqlDataPageBody;
import io.innospots.base.data.dataset.Dataset;
import io.innospots.base.data.dataset.DatasetExecuteParam;
import io.innospots.base.data.dataset.Variable;
import io.innospots.base.data.operator.DataOperatorManager;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.point.IDataOperatorPoint;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.data.request.SimpleRequest;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.Pair;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.data.provider.SqlScriptBuilderManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Alfred
 * @date 2022/1/16
 */
@Slf4j
@Component
public class DataframeExecutor {

    private final IConnectionCredentialReader connectionCredentialReader;

    private DataOperatorManager dataOperatorManager;

    private final DatasetOperator datasetOperator;

    public DataframeExecutor(IConnectionCredentialReader connectionCredentialReader,
                             DataOperatorManager dataOperatorManager,
                             DatasetOperator datasetOperator) {
        this.connectionCredentialReader = connectionCredentialReader;
        this.dataOperatorManager = dataOperatorManager;
        this.datasetOperator = datasetOperator;
    }

    public Dataframe datasetData(ViewExecuteParam viewExecuteParam) {
        if (StringUtils.isEmpty(viewExecuteParam.getViewId()) || (CollectionUtils.isEmpty(viewExecuteParam.getColumns()) &&
                CollectionUtils.isEmpty(viewExecuteParam.getAggregators()) &&
                CollectionUtils.isEmpty(viewExecuteParam.getGroups()))
        ) {
            return Dataframe.empty();
        }

        Dataset dataset = datasetOperator.getDatasetById(Integer.valueOf(viewExecuteParam.getViewId()));
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(dataset.getCredentialKey());
        viewExecuteParam.setScript(dataset.getScript());
        viewExecuteParam.setModel(this.parseSchema(dataset.getModel()));
        viewExecuteParam.setScriptVariables((this.parseVariables(viewExecuteParam, dataset.getVariables())));

        Dataframe dataframe = new Dataframe();
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(dataset.getCredentialKey());
        if (viewExecuteParam.getPageInfo().isCountTotal()) {
            String countSql = SqlScriptBuilderManager.buildCountSql(connectionCredential.getConnectorName(), viewExecuteParam);
            SimpleRequest request = new SimpleRequest(dataset.getCredentialKey(),countSql);
            DataBody<Map<String, Object>> body = dataOperator.execute(request);
            Long total = this.parseResultCount(body);
            viewExecuteParam.getPageInfo().setTotal(total);
            dataframe.setPageInfo(viewExecuteParam.getPageInfo());
        }

        String sql = SqlScriptBuilderManager.buildSql(connectionCredential.getConnectorName(), viewExecuteParam);
        dataframe.setScript(sql);
        SqlDataPageBody pageBody = (SqlDataPageBody<?>) dataOperator.executePage(new SimpleRequest(dataset.getCredentialKey(),sql));
        this.parseResultData(dataframe, pageBody);
        return dataframe;
    }

    public Dataframe datasetData(String credentialKey, int page, int size, DatasetExecuteParam datasetExecuteParam) {
        Dataset dataset = new Dataset();
        dataset.setCredentialKey(credentialKey);
        dataset.setScript(datasetExecuteParam.getScript());
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialKey);

        ViewExecuteParam viewExecuteParam = new ViewExecuteParam();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNo(page);
        pageInfo.setPageSize(size);
        pageInfo.setCountTotal(true);
        viewExecuteParam.setPageInfo(pageInfo);
        viewExecuteParam.setScript(datasetExecuteParam.getScript());
        viewExecuteParam.setParams(datasetExecuteParam.getParams());
        viewExecuteParam.setScriptVariables((this.parseVariables(viewExecuteParam, datasetExecuteParam.getVariables())));
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(dataset.getCredentialKey());

        String countSql = SqlScriptBuilderManager.buildCountSql(connectionCredential.getConnectorName(), viewExecuteParam);

        SimpleRequest request = new SimpleRequest(dataset.getCredentialKey(),countSql);
        DataBody<Map<String, Object>> body = dataOperator.execute(request);
        Long total = this.parseResultCount(body);

        Dataframe dataframe = new Dataframe();
        pageInfo.setTotal(total);
        dataframe.setPageInfo(pageInfo);

        String sql = SqlScriptBuilderManager.buildSql(connectionCredential.getConnectorName(), viewExecuteParam);
        SqlDataPageBody pageBody = (SqlDataPageBody<?>) dataOperator.executePage(new SimpleRequest(dataset.getCredentialKey(),sql));
        dataframe.setScript(sql);
        this.parseResultData(dataframe, pageBody);
        return dataframe;
    }

    public Boolean functionValidate(Integer viewId, String snippet) {
        Dataset dataset = datasetOperator.getDatasetById(viewId);
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(dataset.getCredentialKey());
        return SqlScriptBuilderManager.functionValidate(connectionCredential.getAuthOption(), snippet);
    }

    public Set<StdSqlOperator> supportedFunctions(Integer viewId) {
        Dataset dataset = datasetOperator.getDatasetById(viewId);
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(dataset.getCredentialKey());
        return SqlScriptBuilderManager.supportedFunctions(connectionCredential.getAuthOption());
    }

    private void parseResultData(Dataframe dataframe, SqlDataPageBody<Map<String,Object>> sqlDataPageBody) {
        List<Pair<String, ValueType>> columns = this.convertColumnsValueType(sqlDataPageBody.getSchemaColumns());
        List<Column> resultColumns = new ArrayList<>();

        for (Pair<String, ValueType> column : columns) {
            resultColumns.add(Column.of(column.getRight(), column.getLeft()));
        }
        dataframe.setColumns(resultColumns);

        List<List<Object>> data = new ArrayList<>();
        for (Object item : sqlDataPageBody.getList()) {
            Map<String, Object> map = (Map<String, Object>) item;
            data.add(new ArrayList<>(map.values()));
        }
        dataframe.setRows(data);
    }

    private Long parseResultCount(DataBody<Map<String, Object>> dataBody) {
        if (MapUtils.isNotEmpty(dataBody.getBody())) {
            for (Map.Entry<String, Object> entry : dataBody.getBody().entrySet()) {
                return (Long) entry.getValue();
            }
        }
        return 0L;
    }

    private List<Pair<String, ValueType>> convertColumnsValueType(List<SchemaColumn> schemaColumns) {
        List<Pair<String, ValueType>> columns = new ArrayList<>();

        for (SchemaColumn schemaColumn : schemaColumns) {
            ValueType type;
            switch (schemaColumn.getJdbcType()) {
                case DATE:
                case TIMESTAMP:
                    type = ValueType.DATE;
                    break;
                case BIGINT:
                case INTEGER:
                case DOUBLE:
                    type = ValueType.NUMERIC;
                    break;
                default:
                    type = ValueType.STRING;
                    break;
            }
            columns.add(Pair.of(schemaColumn.getColumnName(), type));
        }
        return columns;
    }

    private Map<String, Column> parseSchema(String model) {
        HashMap<String, Column> schema = new HashMap<>();
        if (StringUtils.isBlank(model)) {
            return schema;
        }

        Map<String, Object> jsonObject = JSONUtils.toMap(model);
        try {
            if (jsonObject.containsKey("columns")) {
                jsonObject = JSONUtils.objectToMap(jsonObject.get("columns"));
                for (String key : jsonObject.keySet()) {
                    Map<String, Object> item = JSONUtils.objectToMap(jsonObject.get(key));
                    String[] names;
                    if (item.get("name") instanceof List) {
                        if (JSONUtils.objectToStrList(JSONUtils.toJsonString(item.get("name"))).size() == 1) {
                            String nameString = JSONUtils.objectToStrList(JSONUtils.toJsonString(item.get("name"))).get(0);
                            try {
                                // TODO 解析有BUG，但不影响程序执行，需要处理
                                names = JSONUtils.objectToStrList(nameString).toArray(new String[0]);
                            } catch (Exception e) {
                                names = new String[]{nameString};
                            }
                        } else {
                            names = JSONUtils.objectToStrList(item.get("name")).toArray(new String[0]);
                        }
                    } else {
                        names = new String[]{String.valueOf(item.get("name"))};
                    }
                    Column column = Column.of(ValueType.valueOf(String.valueOf(item.get("type"))), names);
                    schema.put(column.columnKey(), column);
                }
            } else if (jsonObject.containsKey("hierarchy")) {
                jsonObject = JSONUtils.objectToMap(jsonObject.get("hierarchy"));
                for (String key : jsonObject.keySet()) {
                    Map<String, Object> item = JSONUtils.objectToMap(jsonObject.get(key));
                    if (item.containsKey("children")) {
                        List<String> children = JSONUtils.objectToStrList(item.get("children"));
                        if (children != null && children.size() > 0) {
                            for (int i = 0; i < children.size(); i++) {
                                Map<String, String> child = JSONUtils.toStrMap(children.get(i));
                                schema.put(child.get("name"), Column.of(ValueType.valueOf(child.get("type")), child.get("name").split("\\.")));
                            }
                        }
                    } else {
                        schema.put(key, Column.of(ValueType.valueOf(String.valueOf(item.get("type"))), key.split("\\.")));
                    }
                }
            } else {
                // 兼容1.0.0-beta.1以前的版本
                for (String key : jsonObject.keySet()) {
                    String typeValue = JSONUtils.toStrMap(JSONUtils.toJsonString(jsonObject.get(key))).get("type");
                    ValueType type = ValueType.valueOf(typeValue);
                    schema.put(key, Column.of(type, key));
                }
            }
        } catch (Exception e) {
            log.error("view model parse error", e);
        }
        return schema;
    }

    private List<ScriptVariable> parseVariables(ViewExecuteParam param, List<Variable> variables) {
        //通用变量
        List<ScriptVariable> scriptVariables = new LinkedList<>();

        if (CollectionUtils.isEmpty(variables)) {
            return scriptVariables;
        }

        for (Variable variable : variables) {
            Set<String> values = JSONUtils.toSet(variable.getDefaultValue(), String.class);
            ScriptVariable scriptVariable = new ScriptVariable(
                    variable.getName(),
                    VariableTypeEnum.valueOf(variable.getType()),
                    ValueType.valueOf(variable.getValueType()),
                    values,
                    variable.getExpression()
            );
            scriptVariables.add(scriptVariable);
        }
        scriptVariables.stream()
                .filter(v -> v.getType().equals(VariableTypeEnum.QUERY))
                .forEach(v -> {
                    //通过参数传值，进行参数替换
                    if (!org.springframework.util.CollectionUtils.isEmpty(param.getParams()) && param.getParams().containsKey(v.getName())) {
                        v.setValues(param.getParams().get(v.getName()));
                    } else {
                        //没有参数传值，如果是表达式类型作为默认值，在没有给定值的情况下，改变变量类型为表达式
                        if (v.isExpression()) {
                            v.setValueType(ValueType.FRAGMENT);
                        }
                    }
                });
        return scriptVariables;
    }
}
