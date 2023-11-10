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

package io.innospots.base.converter;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.time.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


/**
 * @author Wren
 * @date 2021-12-25 17:49:00
 */
public interface BaseBeanConverter<Model,Entity> {

    Model entityToModel(Entity entity);

    Entity modelToEntity(Model model);

    List<Model> entitiesToModels(List<Entity> entities);

    List<Entity> modelsToEntities(List<Model> models);


    /**
     * json string to map
     *
     * @param jsonStr
     * @return Map<String, Object>
     */
    default Map<String, Object> jsonStrToMap(String jsonStr) {
        return JSONUtils.toMap(jsonStr, String.class, Object.class);
    }


    /**
     * map to json string
     *
     * @param map
     * @return String
     */
    default String mapToJsonStr(Map<String, Object> map) {
        return JSONUtils.toJsonString(map);
    }

    default Map<String, String> jsonStrToMapStr(String jsonStr) {
        return JSONUtils.toMap(jsonStr, String.class, String.class);
    }


    /**
     * map to json string
     *
     * @param map
     * @return String
     */
    default String mapStrToJsonStr(Map<String, String> map) {
        return JSONUtils.toJsonString(map);
    }

    /**
     * json string to String of list
     *
     * @param jsonStr
     * @return List<String>
     */
    default List<String> jsonStrToList(String jsonStr) {
        return JSONUtils.toList(jsonStr, String.class);
    }

    default List<Map<String, Object>> jsonStrToListMap(String jsonStr) {
        return JSONUtils.toMapList(jsonStr, Map.class);
    }

    /**
     * String of list to json string
     *
     * @param list
     * @return String
     */
    default String listToJsonStr(List<String> list) {
        return JSONUtils.toJsonString(list);
    }

    default String listMapToJsonStr(List<Map<String, Object>> list) {
        return JSONUtils.toJsonString(list);
    }


    /**
     * json string to ParamField of list
     *
     * @param jsonStr
     * @return List<String>
     */
    default List<Integer> jsonToIntList(String jsonStr) {
        return JSONUtils.toList(jsonStr, Integer.class);
    }

    /**
     * ParamField of list to json string
     *
     * @param list
     * @return String
     */
    default String jsonIntToString(List<Integer> list) {
        return JSONUtils.toJsonString(list);
    }


    default String localDateTimeToStr(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return DateTimeUtils.formatLocalDateTime(localDateTime, DateTimeUtils.DATETIME_MS_PATTERN);
    }

    default LocalDateTime strToLocalDateTime(String localDateTimeStr) {
        if (localDateTimeStr == null) {
            return null;
        }
        return LocalDateTime.parse(localDateTimeStr, DateTimeFormatter.ofPattern(DateTimeUtils.DATETIME_MS_PATTERN));
    }
}
