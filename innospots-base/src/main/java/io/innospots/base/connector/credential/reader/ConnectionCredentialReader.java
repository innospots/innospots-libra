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
import io.innospots.base.connector.schema.meta.ConnectionMinderSchemaLoader;
import io.innospots.base.connector.schema.meta.CredentialAuthOption;
import io.innospots.base.crypto.IEncryptor;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.json.JSONUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/4
 */
public class ConnectionCredentialReader implements IConnectionCredentialReader {

    private final IEncryptor encryptor;

    private final CredentialInfoOperator credentialOperator;

    public ConnectionCredentialReader(
            CredentialInfoOperator credentialOperator,
            IEncryptor encryptor) {
        this.credentialOperator = credentialOperator;
        this.encryptor = encryptor;
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
        CredentialType credentialType = credentialInfo.getCredentialType();
        List<CredentialAuthOption> credentialAuthOptions = JSONUtils.toList(credentialType.getAuthOption(),CredentialAuthOption.class);
//        CredentialAuthOption credentialAuthOption = ConnectionMinderSchemaLoader.getCredentialFormConfig(credentialInfo.getConnectorName(), credentialInfo.getCredentialTypeCode());
        CredentialAuthOption credentialAuthOption = credentialAuthOptions.get(0); // TODO 确认一下多个时取哪一个？
        credentialInfo.getProps().putAll(credentialAuthOption.getDefaults());
        ConnectionCredential connection = decryptFormValues(credentialInfo);
        connection.setAuthOption(JSONUtils.toJsonString(credentialAuthOption));
        return connection;
    }

    public CredentialInfo encryptFormValues(CredentialInfo credentialInfo){
        if(MapUtils.isEmpty(credentialInfo.getFormValues())){
            return credentialInfo;
        }
        String jsonStr = JSONUtils.toJsonString(credentialInfo.getFormValues());
        credentialInfo.setEncryptFormValues(encryptor.encode(jsonStr));

        return credentialInfo;
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
            String formValuesStr = encryptor.decode(credentialInfo.getEncryptFormValues());
            connectionCredential.setConfig(JSONUtils.toMap(formValuesStr));
        } catch (Exception e) {
            throw AuthenticationException.buildDecryptException(this.getClass(), "form values");
        }

        return connectionCredential;
    }
}
