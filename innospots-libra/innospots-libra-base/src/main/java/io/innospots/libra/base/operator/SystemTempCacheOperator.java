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

package io.innospots.libra.base.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.store.CacheStore;
import io.innospots.libra.base.dao.SystemTempCacheDao;
import io.innospots.libra.base.entity.SystemTempCacheEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Alfred
 * @date 2023/3/13
 */
@Slf4j
public class SystemTempCacheOperator extends ServiceImpl<SystemTempCacheDao, SystemTempCacheEntity> implements CacheStore {

    public Boolean put(String cacheKey, String cacheValue) {
        SystemTempCacheEntity cache = getByCacheKey(cacheKey);
        if (cache != null) {
            cache.setCacheValue(cacheValue);
        } else {
            cache = new SystemTempCacheEntity();
            cache.setCacheKey(cacheKey);
            cache.setCacheValue(cacheValue);
        }
        super.saveOrUpdate(cache);
        return true;
    }

    @Override
    public void save(String key, String value) {
        SystemTempCacheEntity entity = new SystemTempCacheEntity();
        entity.setCacheKey(key);
        entity.setCacheValue(value);
        super.save(entity);
    }

    public String get(String cacheKey) {
        if (StringUtils.isBlank(cacheKey)) {
            return null;
        }
        SystemTempCacheEntity cache = getByCacheKey(cacheKey);
        return cache == null ? null : cache.getCacheValue();
    }

    @Override
    public boolean remove(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return super.remove(new QueryWrapper<SystemTempCacheEntity>().lambda().eq(SystemTempCacheEntity::getCacheKey,key));
    }

    public void delete(String cacheKey) {
        super.remove(new QueryWrapper<SystemTempCacheEntity>().lambda().eq(SystemTempCacheEntity::getCacheKey, cacheKey));
    }

    private SystemTempCacheEntity getByCacheKey(String cacheKey) {
        List<SystemTempCacheEntity> entities = super.list(new QueryWrapper<SystemTempCacheEntity>()
                .lambda().eq(SystemTempCacheEntity::getCacheKey, cacheKey)
        );
        if (CollectionUtils.isNotEmpty(entities)) {
            if (entities.size() > 1) {
                throw ResourceException.buildUpdateException(this.getClass(), "cache key exists two records, cacheKey: {}", cacheKey);
            }
            return entities.get(0);
        }
        return null;
    }

}
