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

package io.innospots.base.connector.credential.converter;

import io.innospots.base.connector.credential.entity.CredentialTypeEntity;
import io.innospots.base.connector.credential.model.CredentialType;
import io.innospots.base.connector.credential.model.FormValue;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/10/29
 */
@Mapper
public interface CredentialTypeConverter extends BaseBeanConverter<CredentialType, CredentialTypeEntity> {

    CredentialTypeConverter INSTANCE = Mappers.getMapper(CredentialTypeConverter.class);

    @Override
    default CredentialType entityToModel(CredentialTypeEntity entity) {
        CredentialType model = new CredentialType();
        if (StringUtils.isNotBlank(entity.getFormValues())) {
            model.setFormValues(JSONUtils.toList(entity.getFormValues(), FormValue.class));
            entity.setFormValues(null);
        }
        if (StringUtils.isNotBlank(entity.getProps())) {
            model.setProps(JSONUtils.toMap(entity.getProps()));
            entity.setProps(null);
        }
        BeanUtils.copyProperties(entity, model);
        return model;
    }

    @Override
    default CredentialTypeEntity modelToEntity(CredentialType model) {
        CredentialTypeEntity entity = new CredentialTypeEntity();
        BeanUtils.copyProperties(model, entity);
        if (CollectionUtils.isNotEmpty(model.getFormValues())) {
            entity.setFormValues(JSONUtils.toJsonString(model.getFormValues()));
        }
        if (MapUtils.isNotEmpty(model.getProps())) {
            entity.setProps(JSONUtils.toJsonString(model.getProps()));
        }
        return entity;
    }
}
