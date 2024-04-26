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

package io.innospots.base.data.operator;

import cn.hutool.core.util.EnumUtil;
import com.google.common.base.Enums;
import io.innospots.base.condition.Factor;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.enums.DataOperation;
import io.innospots.base.data.operator.jdbc.SelectClause;
import io.innospots.base.data.operator.jdbc.UpdateItem;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.data.request.BatchRequest;
import io.innospots.base.utils.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * data crud operator
 *
 * @author Smars
 * @date 2021/5/3
 */
public interface IDataOperator extends IOperator {


    /**
     * 查询列表
     *
     * @param tableName
     * @param condition
     * @param page
     * @param size
     * @return
     */
    PageBody<Map<String, Object>> selectForList(String tableName, List<Factor> condition, int page, int size);

    PageBody<Map<String, Object>> selectForList(SelectClause selectClause);


    /**
     * 查询最新的数据
     *
     * @param tableName   表或者数据集名称
     * @param upTimeField 时间字段
     * @param size        返回数据量
     * @return
     */
    PageBody<Map<String, Object>> selectLatest(String tableName, String upTimeField, int size);

    /**
     * 查询单条
     *
     * @param tableName
     * @param condition
     * @return
     */
    DataBody<Map<String, Object>> selectForObject(String tableName, List<Factor> condition);

    DataBody<Map<String, Object>> selectForObject(String tableName, String key, String value);

    DataBody<Map<String, Object>> selectForObject(SelectClause selectClause);


    /**
     * 插入数据
     *
     * @param tableName
     * @param data
     * @return
     */
    Integer insert(String tableName, Map<String, Object> data);

    Integer insertBatch(String tableName, List<Map<String, Object>> data);

    /**
     * @param tableName
     * @param keyColumn
     * @param data
     * @return
     */
    Integer upsert(String tableName, String keyColumn, Map<String, Object> data);

    Integer upsertBatch(String tableName, String keyColumn, List<Map<String, Object>> data);


    Integer update(String tableName, UpdateItem item);

    Integer updateForBatch(String tableName, List<UpdateItem> items);

    Integer updateForBatch(String tableName, String keyColumn, List<Map<String, Object>> data);


    Integer delete(String tableName, String field, Object value);

    Integer deleteBatch(
            String tableName,
            List<Factor> condition);


    default Map<String, Object> mapKeyToCamel(Map<String, Object> data) {
        Map<String, Object> resultMap = null;
        if (data != null) {
            resultMap = new HashMap<>();
            for (String key : data.keySet()) {
                resultMap.put(StringConverter.underscoreToCamel(key), data.get(key));
            }
        }
        return resultMap;


    }

    default List<Map<String, Object>> mapKeyToCamel(List<Map<String, Object>> dataList) {
        List<Map<String, Object>> list = null;
        if (dataList != null) {
            list = new ArrayList<>();
            for (Map<String, Object> data : dataList) {
                list.add(mapKeyToCamel(data));
            }
        }
        return list;
    }

    @Override
    default <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        DataOperation dataOperation = Enums.getIfPresent(DataOperation.class, itemRequest.getOperation().toUpperCase()).orNull();
        DataBody dataBody = new DataBody<>();
        Object reqData = itemRequest.getBody();
        int count = 0;
        if (dataOperation != null) {
            switch (dataOperation) {
                case UPSERT:
                    if (reqData instanceof List) {
                        count = this.upsertBatch(itemRequest.getTargetName(), itemRequest.getKeyColumn(), (List<Map<String, Object>>) reqData);
                    } else if (reqData instanceof Map) {
                        count = this.upsert(itemRequest.getTargetName(), itemRequest.getKeyColumn(), (Map<String, Object>) reqData);
                    } else if (reqData instanceof String) {
                        this.execute(reqData.toString());
                    } else {
                        dataBody.setMessage("update data type error");
                    }
                    dataBody.setBody(count);
                    break;
                case INSERT:
                    if (reqData instanceof List) {
                        count = this.insertBatch(itemRequest.getTargetName(), (List<Map<String, Object>>) reqData);
                    } else if (reqData instanceof Map) {
                        count = this.insert(itemRequest.getTargetName(), (Map<String, Object>) reqData);
                    } else if (reqData instanceof String) {
                        this.execute(reqData.toString());
                    } else {
                        dataBody.setMessage("update data type error");
                    }
                    dataBody.setBody(count);
                    break;
                case DELETE:
                    if (itemRequest.getConditions() != null) {
                        count = this.deleteBatch(itemRequest.getTargetName(), itemRequest.getConditions());
                    } else {
                        count = this.delete(itemRequest.getTargetName(), itemRequest.getKeyColumn(), reqData);
                    }
                    dataBody.setBody(count);
                    break;
                case UPDATE:
                    if (reqData instanceof List) {
                        if (((List<?>) reqData).get(0) instanceof UpdateItem) {
                            this.updateForBatch(itemRequest.getTargetName(), (List<UpdateItem>) reqData);
                        } else if (((List<?>) reqData).get(0) instanceof Map) {
                            this.updateForBatch(itemRequest.getTargetName(), itemRequest.getKeyColumn(), (List<Map<String, Object>>) reqData);
                        }
                    } else if (reqData instanceof String) {
                        execute(reqData.toString());
                    } else {
                        dataBody.setMessage("update data type error");
                    }
                    break;
                case GET:
                    if (itemRequest.getConditions() != null) {
                        dataBody = this.selectForObject(itemRequest.getTargetName(), itemRequest.getConditions());
                    } else {
                        dataBody = this.selectForObject(itemRequest.getTargetName(), itemRequest.getKeyColumn(), reqData.toString());
                    }
                    break;
                case LIST:
                    PageBody pageBody = this.selectForList(itemRequest.getTargetName(), itemRequest.getConditions(), itemRequest.getPage(), itemRequest.getSize());
                    dataBody.setBody(pageBody.getList());
                    break;
            }
        } else {
            dataBody.setMessage("operation not support");
        }

        dataBody.end();
        return dataBody;
    }


    @Override
    default <D> PageBody<D> executePage(BaseRequest<?> itemRequest) {
        DataOperation dataOperation = Enums.getIfPresent(DataOperation.class, itemRequest.getOperation().toUpperCase()).orNull();
        PageBody pageBody = new PageBody<>();
        if (dataOperation == DataOperation.LIST) {
            if(itemRequest.getBody() instanceof String){
                return executePage((String)itemRequest.getBody());
            }else{
                pageBody = this.selectForList(itemRequest.getTargetName(), itemRequest.getConditions(), itemRequest.getPage(), itemRequest.getSize());
            }
        } else {
            pageBody.setMessage("operation not support");
        }
        pageBody.end();
        return pageBody;
    }

    default <D> PageBody<D> executePage(String scripts) {
        return null;
    }

    default int execute(String scripts) {
        return 0;
    }

}
