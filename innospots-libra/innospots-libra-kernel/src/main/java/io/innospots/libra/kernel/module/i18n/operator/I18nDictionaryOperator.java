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
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.libra.base.entity.BaseEntity;
import io.innospots.libra.kernel.module.i18n.dao.I18nDictionaryDao;
import io.innospots.libra.kernel.module.i18n.entity.I18nDictionaryEntity;
import io.innospots.libra.kernel.module.i18n.converter.I18nDictionaryConverter;
import io.innospots.libra.kernel.module.i18n.model.I18nDictionary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/9
 */
@Slf4j
@Component
public class I18nDictionaryOperator {


    private final I18nDictionaryDao i18nDictionaryDao;

    public I18nDictionaryOperator(I18nDictionaryDao i18nDictionaryDao) {
        this.i18nDictionaryDao = i18nDictionaryDao;
    }

    /**
     * Create dictionary
     *
     * @param i18nDictionary
     * @return Boolean
     */
    public Boolean create(I18nDictionary i18nDictionary) {
        I18nDictionaryEntity entity = i18nDictionaryDao.selectOne(
                new QueryWrapper<I18nDictionaryEntity>()
                        .lambda().eq(I18nDictionaryEntity::getCode,i18nDictionary.getCode())
        );
        if (entity != null) {
            log.error("create I18nDictionary is exists, {} {}", i18nDictionary.getApp(), i18nDictionary.getCode());
            throw ResourceException.buildExistException(this.getClass(), "i18nDictionary", i18nDictionary);
        }
        entity = I18nDictionaryConverter.INSTANCE.modelToEntity(i18nDictionary);
        int num = i18nDictionaryDao.insert(entity);
        return num == 1;
    }

    /**
     * save or update dictionary
     *
     * @param i18nDictionary
     * @return
     */
    public Boolean saveOrUpdate(I18nDictionary i18nDictionary) {
        I18nDictionaryEntity entity = i18nDictionaryDao.selectOne(
                new QueryWrapper<I18nDictionaryEntity>()
                        .lambda().eq(I18nDictionaryEntity::getCode,i18nDictionary.getCode())
        );
        int num = 0;
        if (entity == null) {
            entity = I18nDictionaryConverter.INSTANCE.modelToEntity(i18nDictionary);
            num = i18nDictionaryDao.insert(entity);
            i18nDictionary.setDictionaryId(entity.getDictionaryId());
        } else {
            i18nDictionary.setDictionaryId(entity.getDictionaryId());
            I18nDictionaryConverter.INSTANCE.updateEntity4Model(entity, i18nDictionary);
            num = i18nDictionaryDao.updateById(entity);
        }
        return num == 1;
    }

    public Boolean delete(Integer dictionaryId) {
        I18nDictionaryEntity entity = i18nDictionaryDao.selectById(dictionaryId);
        if (entity == null) {
            log.error("delete I18nDictionary {} is delete", dictionaryId);
            throw ResourceException.buildUpdateException(this.getClass(), "I18nDictionary is delete", dictionaryId);
        }
        int num = i18nDictionaryDao.deleteById(dictionaryId);
        return num == 1;
    }

    /**
     * Modify dictionary
     *
     * @param i18nDictionary
     * @return
     */
    public Boolean modify(I18nDictionary i18nDictionary) {
        I18nDictionaryEntity entity = i18nDictionaryDao.selectById(i18nDictionary.getDictionaryId());
        if (entity == null) {
            log.error("modify I18nDictionary {} {} is delete", i18nDictionary.getApp(), i18nDictionary.getCode());
            throw ResourceException.buildUpdateException(this.getClass(), "I18nDictionary is delete", i18nDictionary);
        }
        I18nDictionaryConverter.INSTANCE.updateEntity4Model(entity, i18nDictionary);
        int num = i18nDictionaryDao.updateById(entity);
        return num == 1;
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
    public PageBody<I18nDictionary> list(String app, String module, int page, int size) {
        PageBody<I18nDictionary> result = new PageBody<>();
        Page<I18nDictionaryEntity> queryPage = new Page<>(page, size);
        queryPage.addOrder(OrderItem.desc(BaseEntity.F_UPDATED_TIME));
        QueryWrapper<I18nDictionaryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StringUtils.isNoneBlank(app), I18nDictionaryEntity::getApp, app);
        queryWrapper.lambda().eq(StringUtils.isNoneBlank(module), I18nDictionaryEntity::getModule, module);

        queryPage = i18nDictionaryDao.selectPage(queryPage, queryWrapper);
        if (queryPage != null) {
            result.setTotal(queryPage.getTotal());
            result.setCurrent(queryPage.getCurrent());
            result.setPageSize(queryPage.getSize());
            result.setTotalPage(queryPage.getPages());
            result.setList(CollectionUtils.isEmpty(queryPage.getRecords()) ? new ArrayList<I18nDictionary>() :
                    I18nDictionaryConverter.INSTANCE.entityToModelList(queryPage.getRecords()));
        }
        return result;
    }


    public List<String> listApps() {
        return i18nDictionaryDao.selectApps();
    }


    public List<String> listModulesByAppName(String appName) {
        return i18nDictionaryDao.selectModulesByAppName(appName);
    }

    public List<String> listModules() {
        return i18nDictionaryDao.selectModules();
    }

}
