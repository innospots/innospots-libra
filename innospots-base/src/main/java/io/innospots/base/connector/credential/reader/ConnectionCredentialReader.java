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

package io.innospots.base.connector.credential.reader;

import io.innospots.base.connector.credential.converter.CredentialInfoConverter;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.credential.model.CredentialInfo;
import io.innospots.base.connector.credential.model.CredentialType;
import io.innospots.base.connector.credential.operator.CredentialInfoOperator;
import io.innospots.base.connector.credential.operator.CredentialTypeOperator;
import io.innospots.base.connector.meta.ConnectionMinderSchemaLoader;
import io.innospots.base.connector.meta.CredentialAuthOption;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.exception.ResourceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/4
 */
@Slf4j
public class ConnectionCredentialReader implements IConnectionCredentialReader {

    private final CredentialInfoOperator credentialOperator;

    private final CredentialTypeOperator credentialTypeOperator;

    public ConnectionCredentialReader(CredentialInfoOperator credentialOperator,
                                      CredentialTypeOperator credentialTypeOperator) {
        this.credentialOperator = credentialOperator;
        this.credentialTypeOperator = credentialTypeOperator;
    }

    @Override
    public ConnectionCredential readCredential(String credentialKey) {
        CredentialInfo credentialInfo = credentialOperator.getCredential(credentialKey);
        return this.fillCredential(credentialInfo);
    }

    @Override
    public List<CredentialInfo> readCredentialInfos(Set<String> credentialKeys) {
        return credentialOperator.listCredentialInfos(credentialKeys);
    }


    @Override
    public ConnectionCredential fillCredential(CredentialInfo credentialInfo) {
        if (credentialInfo == null) {
            return null;
        }
        CredentialType credentialType = credentialTypeOperator.getCredentialType(credentialInfo.getCredentialTypeCode());
        if (credentialType == null) {
            log.error("oauth2 credential callback credentialTypeCode invalid");
            throw ResourceException.buildNotExistException(this.getClass(),"oauth2-credential", "oauth2 credential callback credentialTypeCode invalid");
        }
        if(credentialInfo.getProps()==null){
            credentialInfo.setProps(new LinkedHashMap<>());
        }
        credentialInfo.setCredentialType(credentialType);
        credentialInfo.setConnectorName(credentialType.getConnectorName());
        if(credentialType.getProps()!=null){
            credentialInfo.getProps().putAll(credentialType.getProps());
        }
        CredentialAuthOption credentialAuthOption = ConnectionMinderSchemaLoader.getCredentialFormConfig(credentialInfo.getConnectorName(), credentialInfo.getCredentialType().getAuthOption());
        if(credentialAuthOption!=null && credentialAuthOption.getDefaults()!=null){
            credentialInfo.getProps().putAll(credentialAuthOption.getDefaults());
        }
        ConnectionCredential connection = decryptFormValues(credentialInfo);
        connection.setAuthOption(credentialInfo.getCredentialType().getAuthOption());
        return connection;
    }


    private ConnectionCredential decryptFormValues(CredentialInfo credentialInfo) {
        if (credentialInfo == null) {
            return null;
        }

        ConnectionCredential connectionCredential =
                CredentialInfoConverter.INSTANCE.credentialToConnection(credentialInfo);
        if (StringUtils.isBlank(credentialInfo.getEncryptFormValues())) {
            connectionCredential.setConfig(new HashMap<>());
            return connectionCredential;
        }
        try {
            credentialInfo = credentialOperator.decryptFormValues(credentialInfo);
            connectionCredential.setConfig(credentialInfo.getFormValues());
        } catch (Exception e) {
            throw AuthenticationException.buildDecryptException(this.getClass(), "form values");
        }

        return connectionCredential;
    }
}
