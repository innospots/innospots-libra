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
import io.innospots.libra.kernel.module.i18n.entity.I18nTransMessageEntity;
import io.innospots.libra.kernel.module.i18n.model.LocaleMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/9
 */
public interface I18nTransMessageDao extends BaseMapper<I18nTransMessageEntity> {


    /**
     * select local message by code of dictionary
     *
     * @param dictionaryCode
     * @param locale
     * @return LocaleMessage
     */
    @Select("select msg.locale as locale, msg.message as message, dict.code as code from i18n_trans_message as msg, i18n_dictionary as dict where dict.dictionary_id = msg.dictionary_id and dict.code = #{dictionaryCode} and msg.locale=#{locale}")
    LocaleMessage selectLocalMessageByCode(@Param("dictionaryCode") String dictionaryCode, @Param("locale") String locale);

    /**
     * select local message List by app of dictionary
     *
     * @param dictionaryApp
     * @param locale
     * @return List<LocaleMessage>
     */
    @Select("select msg.locale as locale, msg.message as message, dict.code as code from i18n_trans_message as msg, i18n_dictionary as dict where dict.dictionary_id = msg.dictionary_id and dict.app = #{dictionaryApp} and msg.locale=#{locale}")
    List<LocaleMessage> selectLocalMessageByApp(@Param("dictionaryApp") String dictionaryApp, @Param("locale") String locale);


    /**
     * Query the transMessage according to dictionaryId
     *
     * @param dictionaryId
     * @return
     */
    @Select("select * from " + I18nTransMessageEntity.TABLE_NAME + " where dictionary_id = #{dictionaryId}")
    List<I18nTransMessageEntity> selectByDictionaryId(Integer dictionaryId);

    /**
     * Query the transMessage according to dictionaryIds
     *
     * @param dictionaryIds
     * @return
     */
    @Select({
            "<script>",
            "select * from " + I18nTransMessageEntity.TABLE_NAME + " where dictionary_id in",
            "<foreach collection='dictionaryIds' item='item' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<I18nTransMessageEntity> selectByDictionaryIds(List<Integer> dictionaryIds);
}
