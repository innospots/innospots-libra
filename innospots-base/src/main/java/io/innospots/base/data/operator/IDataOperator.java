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

import io.innospots.base.condition.Factor;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.operator.jdbc.SelectClause;
import io.innospots.base.data.operator.jdbc.UpdateItem;
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


    Integer delete(String tableName, String field,Object value);

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

}
