/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.libra.kernel.module.schema.dataset;

import io.innospots.base.connector.credential.CredentialInfo;
import io.innospots.base.connector.credential.IConnectionCredentialReader;
import io.innospots.base.connector.schema.SchemaRegistry;
import io.innospots.base.connector.schema.SchemaRegistryType;
import io.innospots.base.connector.schema.config.ConnectionMinderSchema;
import io.innospots.base.connector.schema.config.ConnectionMinderSchemaLoader;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.dataset.Dataset;
import io.innospots.base.data.dataset.IDatasetReader;
import io.innospots.base.exception.ResourceException;
import io.innospots.libra.kernel.module.credential.entity.CredentialInfoEntity;
import io.innospots.libra.kernel.module.credential.operator.CredentialInfoOperator;
import io.innospots.libra.kernel.module.schema.converter.SchemaRegistryBeanConverter;
import io.innospots.libra.kernel.module.schema.operator.SchemaRegistryOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alfred
 * @date 2022/1/31
 */
public class DatasetOperator implements IDatasetReader {

    private final SchemaRegistryOperator schemaRegistryOperator;

    private final IConnectionCredentialReader connectionCredentialReader;

    public DatasetOperator(SchemaRegistryOperator schemaRegistryOperator,
                           IConnectionCredentialReader connectionCredentialReader) {
        this.schemaRegistryOperator = schemaRegistryOperator;
        this.connectionCredentialReader = connectionCredentialReader;
    }

    @Transactional(rollbackFor = Exception.class)
    public Dataset createDataset(Dataset dataset) {
        if (schemaRegistryOperator.checkNameExist(dataset.getName(),dataset.getId())) {
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
        if (schemaRegistryOperator.checkNameExist(dataset.getName(), dataset.getId())) {
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


    @Override
    public List<Dataset> listDatasets(String credentialKey) {
        List<SchemaRegistry> schemaRegistries = schemaRegistryOperator.listSchemaRegistries(credentialKey);
        return SchemaRegistryBeanConverter.INSTANCE.schemaRegistriesToDatasets(schemaRegistries);
    }

    @Override
    public List<Dataset> listDatasets(Set<Integer> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyList();
        }
        List<SchemaRegistry> schemaRegistries = schemaRegistryOperator.listByRegistryIds(keys);
        return SchemaRegistryBeanConverter.INSTANCE.schemaRegistriesToDatasets(schemaRegistries);
    }

    private void fillDatasetIcon(List<Dataset> datasets){
        Set<String> keys = datasets.stream().map(Dataset::getCredentialKey).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        List<CredentialInfo> credentials = connectionCredentialReader.readCredentialInfos(keys);
        Map<String, String> iconMap = new HashMap<>();
        for (CredentialInfo credential : credentials) {
            ConnectionMinderSchema minderSchema = ConnectionMinderSchemaLoader.getConnectionMinderSchema(credential.getCredentialTypeCode());
            iconMap.put(credential.getCredentialKey(), minderSchema.getIcon());
        }
        datasets.forEach(v -> v.setIcon(iconMap.get(v.getCredentialKey())));
    }

}
