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

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.credential.model.CredentialInfo;
import io.innospots.base.connector.credential.entity.CredentialInfoEntity;
import io.innospots.base.connector.credential.entity.CredentialTypeEntity;
import io.innospots.base.connector.credential.model.SimpleCredentialInfo;
import io.innospots.base.converter.BaseBeanConverter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * @author Smars
 */
@Mapper
public interface CredentialInfoConverter extends BaseBeanConverter<CredentialInfo, CredentialInfoEntity> {

    CredentialInfoConverter INSTANCE = Mappers.getMapper(CredentialInfoConverter.class);


    ConnectionCredential credentialToConnection(CredentialInfo credentialInfo);

    SimpleCredentialInfo credentialToSimple(CredentialInfo credentialInfo);

    SimpleCredentialInfo entityToSimpleModel(CredentialInfoEntity credentialEntity);

    default SimpleCredentialInfo entityToSimpleModel(CredentialInfoEntity credentialEntity, CredentialTypeEntity credentialTypeEntity){
        SimpleCredentialInfo credentialInfo = entityToSimpleModel(credentialEntity);
        if(credentialTypeEntity!=null){
            credentialInfo.setIcon(credentialTypeEntity.getIcon());
            credentialInfo.setCredentialTypeCode(credentialTypeEntity.getTypeCode());
            credentialInfo.setConnectorName(credentialTypeEntity.getConnectorName());
        }

        return credentialInfo;
    }

}
