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

package io.innospots.libra.kernel.module.i18n.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.innospots.libra.kernel.module.i18n.entity.I18nDictionaryEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/9
 */
public interface I18nDictionaryDao extends BaseMapper<I18nDictionaryEntity> {


    /**
     * @return
     */
    @Select("select DISTINCT(app) from " + I18nDictionaryEntity.TABLE_NAME)
    List<String> selectApps();


    /**
     * @param appName
     * @return
     */
    @Select("select distinct(module) from " + I18nDictionaryEntity.TABLE_NAME + " where app=#{appName}")
    List<String> selectModulesByAppName(String appName);

    @Select("select distinct(module) from " + I18nDictionaryEntity.TABLE_NAME)
    List<String> selectModules();
}
