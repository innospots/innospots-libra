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

import io.innospots.base.condition.Factor;
import io.innospots.base.data.ap.IDataOperatorPoint;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.operator.UpdateItem;
import io.innospots.base.model.DataBody;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.RequestBody;
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
public class DataOperatorPointEndPoint implements IDataOperatorPoint {

    private final DataOperatorManager dataOperatorManager;

    public DataOperatorPointEndPoint(DataOperatorManager dataOperatorManager) {
        this.dataOperatorManager = dataOperatorManager;
    }

    @Override
    public InnospotResponse<PageBody<Map<String, Object>>> queryForList(Integer credentialId, String tableName, List<Factor> condition, int page, int size) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        return InnospotResponse.success(dataOperator.selectForList(tableName, condition, page, size));
    }


    @Override
    public InnospotResponse<DataBody<Map<String, Object>>> queryForObject(Integer credentialId, String tableName,
                                                                          List<Factor> condition) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        return InnospotResponse.success(dataOperator.selectForObject(tableName, condition));
    }

    @Override
    public InnospotResponse<Integer> insert(Integer credentialId, String tableName,
                                            Map<String, Object> data) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        return InnospotResponse.success(dataOperator.insert(tableName, data));
    }

    @Override
    public InnospotResponse<Integer> insertBatch(Integer credentialId, String tableName, List<Map<String, Object>> data) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        return InnospotResponse.success(dataOperator.insertBatch(tableName, data));
    }

    @Override
    public InnospotResponse<Integer> upsert(Integer credentialId, String tableName, String keyColumn, Map<String, Object> data) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        return InnospotResponse.success(dataOperator.upsert(tableName, keyColumn, data));
    }

    @Override
    public InnospotResponse<Integer> upsertForBatch(Integer credentialId, String tableName, String keyColumn, List<Map<String, Object>> data) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        return InnospotResponse.success(dataOperator.upsertBatch(tableName, keyColumn, data));
    }

    @Override
    public InnospotResponse<Integer> update(Integer credentialId, String tableName, UpdateItem item) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        //return dataOperator.update(tableName, item);
        // TODO
        return null;
    }

    @Override
    public InnospotResponse<Integer> updateForBatch(Integer datasourceId, String tableName,
                                                    List<UpdateItem> items) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(datasourceId);
        //return dataOperator.updateForBatch(tableName, items);
        // TODO
        return null;
    }

    @Override
    public InnospotResponse<Integer> delete(Integer credentialId,
                                            String tableName,
                                            List<Factor> condition) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        // TODO
        return null;
    }

    @Override
    public InnospotResponse<Integer> deleteBatch(Integer credentialId,
                                                 String tableName,
                                                 List<Factor> condition
    ) {
        IDataOperator dataOperator = dataOperatorManager.buildDataOperator(credentialId);
        return InnospotResponse.success(dataOperator.deleteBatch(tableName, condition));
    }

    @Override
    public InnospotResponse<DataBody> execute(RequestBody requestBody) {
        IExecutionOperator dataOperator = dataOperatorManager.buildExecutionOperator(requestBody.getCredentialId(),requestBody.getConnectorName());
        return InnospotResponse.success(dataOperator.execute(requestBody));
    }
}
