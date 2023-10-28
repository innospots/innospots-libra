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

package io.innospots.connector.schema.endpoint;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.connector.schema.operator.DataOperatorManager;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Alfred
 * @date 2021-05-10
 */
@RestController
public class SqlOperatorPointEndPoint implements ISqlOperatorPoint {

    private final DataOperatorManager dataOperatorManager;

    public SqlOperatorPointEndPoint(DataOperatorManager dataOperatorManager) {
        this.dataOperatorManager = dataOperatorManager;
    }

    @Override
    public InnospotResponse<DataBody<Map<String, Object>>> queryForObject(Integer datasourceId, String sql) {
        ISqlOperator sqlOperator = dataOperatorManager.buildSqlOperator(datasourceId);
        return sqlOperator.selectForObject(sql);
    }

    @Override
    public InnospotResponse<PageBody> queryForList(Integer datasourceId, String sql) {
        ISqlOperator sqlOperator = dataOperatorManager.buildSqlOperator(datasourceId);
        return sqlOperator.selectForList(sql);
    }

    @Override
    public InnospotResponse<PageBody> queryForList(Integer datasourceId, int page, int size, String sql) {
        ISqlOperator sqlOperator = dataOperatorManager.buildSqlOperator(datasourceId);
        return sqlOperator.selectForList(sql, page, size);
    }

    @Override
    public InnospotResponse<Integer> executeForSql(Integer datasourceId, String sql) {
        ISqlOperator sqlOperator = dataOperatorManager.buildSqlOperator(datasourceId);
        return sqlOperator.executeForSql(sql);
    }

    @Override
    public InnospotResponse<Integer> executeForSqlBatch(Integer datasourceId, List<String> sql) {
        ISqlOperator sqlOperator = dataOperatorManager.buildSqlOperator(datasourceId);
        return sqlOperator.executeForSqlBatch(sql);
    }
}
