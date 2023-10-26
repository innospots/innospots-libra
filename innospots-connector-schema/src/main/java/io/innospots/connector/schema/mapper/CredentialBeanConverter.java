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

package io.innospots.connector.schema.mapper;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.data.schema.AppCredentialInfo;
import io.innospots.base.data.schema.SimpleAppCredential;
import io.innospots.connector.schema.entity.AppCredentialEntity;
import io.innospots.base.data.schema.ConnectionCredential;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * @author Smars
 */
@Mapper
public interface CredentialBeanConverter extends BaseBeanConverter {

    CredentialBeanConverter INSTANCE = Mappers.getMapper(CredentialBeanConverter.class);

    AppCredentialEntity modelToEntity(AppCredentialInfo credential);

    ConnectionCredential credentialToConnection(AppCredentialInfo credential);

    AppCredentialInfo entityToModel(AppCredentialEntity appCredentialEntity);

    List<AppCredentialInfo> entitiesToModels(List<AppCredentialEntity> credentialEntities);

    SimpleAppCredential credentialToSimple(AppCredentialInfo appCredentialInfo);

    SimpleAppCredential entityToSimpleModel(AppCredentialEntity appCredentialEntity);

}
