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

package io.innospots.connector.schema.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.data.dataset.Dataset;
import io.innospots.base.data.dataset.IDatasetReader;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.data.schema.SchemaRegistryType;
import io.innospots.base.data.schema.config.ConnectionMinderSchema;
import io.innospots.base.data.schema.config.ConnectionMinderSchemaLoader;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.data.body.PageBody;
import io.innospots.connector.schema.dao.SchemaRegistryDao;
import io.innospots.connector.schema.entity.AppCredentialEntity;
import io.innospots.connector.schema.entity.SchemaRegistryEntity;
import io.innospots.connector.schema.mapper.SchemaRegistryBeanConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2022/1/31
 */
public class DatasetOperator extends ServiceImpl<SchemaRegistryDao, SchemaRegistryEntity> implements IDatasetReader {

    private final SchemaRegistryOperator schemaRegistryOperator;

    private final AppCredentialOperator appCredentialOperator;

    public DatasetOperator(SchemaRegistryOperator schemaRegistryOperator,
                           AppCredentialOperator appCredentialOperator) {
        this.schemaRegistryOperator = schemaRegistryOperator;
        this.appCredentialOperator = appCredentialOperator;
    }

    @Transactional(rollbackFor = Exception.class)
    public Dataset createDataset(Dataset dataset) {
        if (this.checkNameExist(dataset.getName())) {
            throw ResourceException.buildExistException(this.getClass(), dataset.getName());
        }
        // set default categoryId
        if (dataset.getCategoryId() == null) {
            dataset.setCategoryId(0);
        }

        SchemaRegistry schemaRegistry = schemaRegistryOperator.createSchemaRegistry(SchemaRegistryBeanConverter.INSTANCE.datasetToSchemaRegistry(dataset));
        return SchemaRegistryBeanConverter.INSTANCE.schemaRegistryToDataset(schemaRegistry);
    }

    @Transactional(rollbackFor = Exception.class)
    public Dataset updateDataset(Dataset dataset) {
        if (this.checkNameExistAndExcludeOriginalName(dataset.getName(), dataset.getId())) {
            throw ResourceException.buildExistException(this.getClass(), dataset.getName());
        }
        // set default categoryId
        if (dataset.getCategoryId() == null) {
            dataset.setCategoryId(0);
        }
        SchemaRegistry schemaRegistry = schemaRegistryOperator.updateSchemaRegistry(SchemaRegistryBeanConverter.INSTANCE.datasetToSchemaRegistry(dataset));
        return SchemaRegistryBeanConverter.INSTANCE.schemaRegistryToDataset(schemaRegistry);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDataset(Integer registryId) {
        return schemaRegistryOperator.deleteSchemaRegistry(registryId);
    }

    public Dataset getDatasetById(Integer registryId) {
        return SchemaRegistryBeanConverter.INSTANCE.schemaRegistryToDataset(schemaRegistryOperator.getSchemaRegistryById(registryId, true));
    }

    public List<Dataset> listDatasets(Integer categoryId, String queryCode, String sort) {
        List<SchemaRegistry> schemaRegistries = schemaRegistryOperator.listSchemaRegistries(queryCode, sort, categoryId, SchemaRegistryType.DATASET);
        List<Dataset> datasets = SchemaRegistryBeanConverter.INSTANCE.schemaRegistriesToDatasets(schemaRegistries);
        this.fillDatasetIcon(datasets);
        return datasets;
    }

    public PageBody<Dataset> pageDatasets(Integer categoryId, Integer page, Integer size, String queryCode, String sort) {
        PageBody<SchemaRegistry> schemaRegistryPageBody = schemaRegistryOperator.pageSchemaRegistries(queryCode, sort, categoryId, SchemaRegistryType.DATASET, page, size);
        List<Dataset> datasets = SchemaRegistryBeanConverter.INSTANCE.schemaRegistriesToDatasets(schemaRegistryPageBody.getList());
        PageBody<Dataset> pageBody = new PageBody<>();
        this.fillDatasetIcon(datasets);
        pageBody.setList(datasets);
        pageBody.setPageSize(schemaRegistryPageBody.getPagination().getPageSize());
        pageBody.setCurrent(schemaRegistryPageBody.getPagination().getCurrent());
        pageBody.setTotal(schemaRegistryPageBody.getPagination().getTotal());
        return pageBody;
    }

    private boolean checkNameExist(String name) {
        return super.count(new QueryWrapper<SchemaRegistryEntity>().lambda()
                .eq(SchemaRegistryEntity::getName, name)
                .eq(SchemaRegistryEntity::getRegistryType, SchemaRegistryType.DATASET)) > 0;
    }

    private boolean checkNameExistAndExcludeOriginalName(String name, Integer registryId) {
        return super.count(new QueryWrapper<SchemaRegistryEntity>().lambda()
                .eq(SchemaRegistryEntity::getName, name)
                .eq(SchemaRegistryEntity::getRegistryType, SchemaRegistryType.DATASET)
                .ne(SchemaRegistryEntity::getRegistryId, registryId)) > 0;
    }

    @Override
    public List<Dataset> listDatasets(Integer credentialId) {
        List<SchemaRegistry> schemaRegistries = schemaRegistryOperator.listSchemaRegistries(credentialId);
        return SchemaRegistryBeanConverter.INSTANCE.schemaRegistriesToDatasets(schemaRegistries);
    }

    @Override
    public List<Dataset> listDatasets(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<SchemaRegistry> schemaRegistries = schemaRegistryOperator.listByRegistryIds(ids);
        return SchemaRegistryBeanConverter.INSTANCE.schemaRegistriesToDatasets(schemaRegistries);
    }

    private void fillDatasetIcon(List<Dataset> datasets){
        Set<Integer> ids = datasets.stream().map(Dataset::getCredentialId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<AppCredentialEntity> credentials = appCredentialOperator.listByIds(ids);
        Map<Integer, String> iconMap = new HashMap<>();
        for (AppCredentialEntity credential : credentials) {
            ConnectionMinderSchema minderSchema = ConnectionMinderSchemaLoader.getConnectionMinderSchema(credential.getConnectorName());
            iconMap.put(credential.getCredentialId(), minderSchema.getIcon());
        }
        datasets.forEach(v -> v.setIcon(iconMap.get(v.getCredentialId())));
    }

}
