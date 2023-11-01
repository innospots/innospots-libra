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

package io.innospots.libra.kernel.module.i18n.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.innospots.base.enums.DataStatus;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.libra.base.entity.BaseEntity;
import io.innospots.libra.kernel.module.i18n.dao.I18nDictionaryDao;
import io.innospots.libra.kernel.module.i18n.dao.I18nLanguageDao;
import io.innospots.libra.kernel.module.i18n.dao.I18nTransMessageDao;
import io.innospots.libra.kernel.module.i18n.entity.I18nDictionaryEntity;
import io.innospots.libra.kernel.module.i18n.entity.I18nLanguageEntity;
import io.innospots.libra.kernel.module.i18n.entity.I18nTransMessageEntity;
import io.innospots.libra.kernel.module.i18n.converter.I18NLanguageBeanConverter;
import io.innospots.libra.kernel.module.i18n.converter.I18nDictionaryConvertMapper;
import io.innospots.libra.kernel.module.i18n.model.I18nTransMessageGroup;
import io.innospots.libra.kernel.module.i18n.model.TransHeaderColumn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/9
 */
@Slf4j
@Component
public class I18nTransMessageOperator {

    private I18nLanguageDao i18nLanguageDao;

    private I18nDictionaryDao i18nDictionaryDao;

    private I18nTransMessageDao i18nTransMessageDao;

    public I18nTransMessageOperator(I18nLanguageDao i18nLanguageDao, I18nDictionaryDao i18nDictionaryDao, I18nTransMessageDao i18nTransMessageDao) {
        this.i18nLanguageDao = i18nLanguageDao;
        this.i18nDictionaryDao = i18nDictionaryDao;
        this.i18nTransMessageDao = i18nTransMessageDao;
    }

    /**
     * get TransHeaderColumn List
     *
     * @return
     */
    public List<TransHeaderColumn> transHeaderColumns() {
        QueryWrapper<I18nLanguageEntity> languageQueryWrapper = new QueryWrapper<>();
        languageQueryWrapper.in("status", DataStatus.ONLINE.name(), DataStatus.OFFLINE.name());
        languageQueryWrapper.orderByDesc(BaseEntity.F_UPDATED_TIME);
        List<I18nLanguageEntity> languageEntityList = i18nLanguageDao.selectList(languageQueryWrapper);
        List<TransHeaderColumn> result = new ArrayList<>();
        if (languageEntityList != null && !languageEntityList.isEmpty()) {
            languageEntityList.forEach(entity -> {
                if (entity.isDefaultLan()) {
                    result.add(0, I18NLanguageBeanConverter.INSTANCE.entityToTransHeaderColumn(entity));
                } else {
                    result.add(I18NLanguageBeanConverter.INSTANCE.entityToTransHeaderColumn(entity));
                }
            });
        }
        return result;
    }


    @CacheEvict(cacheNames = "locale_resource", allEntries = true)
    @Transactional(rollbackForClassName = {"Exception"})
    public Boolean updateTransMessageGroup(I18nTransMessageGroup transMessages) {
        //先获取字典
        I18nDictionaryEntity dictionaryEntity = i18nDictionaryDao.selectByCode(transMessages.getDictionary().getCode());
        if (dictionaryEntity == null) {
            log.error("modify I18nTransMessageGroup {} is not exist", transMessages.getDictionary().getCode());
            throw ResourceException.buildUpdateException(this.getClass(), "modify I18nTransMessageGroup {} is not exist", transMessages.getDictionary().getCode());
        }

        int resultNum = 0;
        int messageNum = 0;
        Map<String, I18nTransMessageEntity> entityMap = new HashMap<>();
        List<I18nTransMessageEntity> entityList = i18nTransMessageDao.selectByDictionaryId(dictionaryEntity.getDictionaryId());
        if (entityList != null && !entityList.isEmpty()) {
            entityMap = entityList.stream().collect(Collectors.toMap(I18nTransMessageEntity::getLocale, Function.identity()));
        }
        if (null != transMessages.getMessages()) {
            messageNum = transMessages.getMessages().size();
            String messageValue = "";
            for (Map.Entry<String, String> entry : transMessages.getMessages().entrySet()) {
                messageValue = entry.getValue();
                //做字段长度保护
                if (messageValue != null && messageValue.length() > 512) {
                    messageValue = messageValue.substring(0, 512);
                }
                if (entityMap.containsKey(entry.getKey())) {
                    I18nTransMessageEntity i18nTransMessageEntity = entityMap.get(entry.getKey());
                    i18nTransMessageEntity.setMessage(messageValue);
                    resultNum += i18nTransMessageDao.updateById(i18nTransMessageEntity);
                } else {
                    resultNum += i18nTransMessageDao.insert(new I18nTransMessageEntity(dictionaryEntity.getDictionaryId(), entry.getKey(), messageValue));
                }
            }
        }
        if (messageNum != resultNum) {
            log.error("modify I18nTransMessageGroup {} error messageNum:{} resultNum:{}", transMessages.getDictionary().getCode(), messageNum, resultNum);
            throw ResourceException.buildUpdateException(this.getClass(), "modify I18nTransMessageGroup {} error messageNum:{} resultNum:{}", transMessages.getDictionary().getCode(), messageNum, resultNum);
        }
        return true;
    }


    /**
     * Paging query dictionary
     *
     * @param app
     * @param module
     * @param page
     * @param size
     * @return
     */
    public PageBody<I18nTransMessageGroup> pageTranslations(String app, String module, String code, int page, int size) {
        PageBody<I18nTransMessageGroup> result = new PageBody<>();
        Page<I18nDictionaryEntity> queryPage = new Page<>(page, size);
        queryPage.addOrder(OrderItem.desc(BaseEntity.F_UPDATED_TIME));
        QueryWrapper<I18nDictionaryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNoneBlank(app), "app", app);
        queryWrapper.eq(StringUtils.isNoneBlank(module), "module", module);
        queryWrapper.like(StringUtils.isNoneBlank(code), "code", code);

        List<Integer> dictionaryIds = new ArrayList<>();
        queryPage = i18nDictionaryDao.selectPage(queryPage, queryWrapper);
        if (queryPage != null) {
            List<I18nTransMessageGroup> list = new ArrayList<>();
            for (I18nDictionaryEntity entity : queryPage.getRecords()) {
                dictionaryIds.add(entity.getDictionaryId());
                list.add(new I18nTransMessageGroup(I18nDictionaryConvertMapper.INSTANCE.entityToModel(entity), new HashMap<String, String>()));
            }
            I18nDictionaryConvertMapper.INSTANCE.entityToModelList(queryPage.getRecords());
            result.setTotal(queryPage.getTotal());
            result.setCurrent(queryPage.getCurrent());
            result.setPageSize(queryPage.getSize());
            result.setTotalPage(queryPage.getPages());
            result.setList(CollectionUtils.isEmpty(queryPage.getRecords()) ? new ArrayList<I18nTransMessageGroup>() :
                    list);
        }

        Map<Integer, Map<String, String>> resMap = null;
        if (!dictionaryIds.isEmpty()) {
            List<I18nTransMessageEntity> entityList = i18nTransMessageDao.selectByDictionaryIds(dictionaryIds);
            if (entityList != null) {
                resMap = new HashMap<>();
                for (I18nTransMessageEntity entity : entityList) {
                    if (!resMap.containsKey(entity.getDictionaryId())) {
                        resMap.put(entity.getDictionaryId(), new HashMap<>());
                    }
                    resMap.get(entity.getDictionaryId()).put(entity.getLocale(), entity.getMessage());
                }
            }
        }

        if (result.getList() != null && resMap != null) {
            for (I18nTransMessageGroup group : result.getList()) {
                if (resMap.containsKey(group.getDictionary().getDictionaryId())) {
                    group.setMessages(resMap.get(group.getDictionary().getDictionaryId()));
                }
            }
        }

        return result;
    }


}
