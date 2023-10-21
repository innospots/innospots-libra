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

package io.innospots.connector.schema.reader;

import io.innospots.base.crypto.EncryptType;
import io.innospots.base.crypto.EncryptorBuilder;
import io.innospots.base.crypto.IEncryptor;
import io.innospots.base.data.schema.AppCredentialInfo;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.data.schema.config.ConnectionMinderSchemaLoader;
import io.innospots.base.data.schema.config.CredentialFormConfig;
import io.innospots.base.data.schema.reader.IConnectionCredentialReader;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.json.JSONUtils;
import io.innospots.connector.schema.mapper.CredentialConvertMapper;
import io.innospots.connector.schema.operator.AppCredentialOperator;
import io.innospots.libra.base.configuration.AuthProperties;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/4
 */
public class ConnectionCredentialReader implements IConnectionCredentialReader {

    private final IEncryptor encryptor;

    private final AppCredentialOperator credentialOperator;

    public ConnectionCredentialReader(
            AppCredentialOperator credentialOperator,
            AuthProperties authProperties) {
        this.credentialOperator = credentialOperator;
        encryptor = EncryptorBuilder.build(EncryptType.BLOWFISH, authProperties.getSecretKey());
    }

    @Override
    public ConnectionCredential readCredential(Integer credentialId) {
        AppCredentialInfo appCredentialInfo = credentialOperator.getCredential(credentialId);
        return fillCredential(appCredentialInfo);
    }

    @Override
    public ConnectionCredential readCredential(String credentialCode) {
        AppCredentialInfo appCredentialInfo = credentialOperator.getCredentialByCode(credentialCode);
        return fillCredential(appCredentialInfo);
    }

    @Override
    public ConnectionCredential fillCredential(AppCredentialInfo appCredentialInfo) {
        if (appCredentialInfo == null) {
            return null;
        }
        // decrypt formValues
        ConnectionCredential connectionCredential = this.decryptFormValues(appCredentialInfo);
        if (connectionCredential == null) {
            return null;
        }
        CredentialFormConfig credentialFormConfig = ConnectionMinderSchemaLoader.getCredentialFormConfig(connectionCredential.getConnectorName(), connectionCredential.getConfigCode());
        connectionCredential.getConfig().putAll(credentialFormConfig.getDefaults());
        return connectionCredential;
    }

    public AppCredentialInfo encryptFormValues(AppCredentialInfo appCredentialInfo){
        if(MapUtils.isEmpty(appCredentialInfo.getFormValues())){
            return appCredentialInfo;
        }
        String jsonStr = JSONUtils.toJsonString(appCredentialInfo.getFormValues());
        appCredentialInfo.setEncryptFormValues(encryptor.encode(jsonStr));

        return appCredentialInfo;
    }

    private ConnectionCredential decryptFormValues(AppCredentialInfo appCredentialInfo) {
        if (appCredentialInfo == null) {
            return null;
        }

        ConnectionCredential connectionCredential =
                CredentialConvertMapper.INSTANCE.credentialToConnection(appCredentialInfo);

        if (StringUtils.isBlank(appCredentialInfo.getEncryptFormValues())) {
            connectionCredential.setConfig(new HashMap<>());
            return connectionCredential;
        }
        try {
            String formValuesStr = encryptor.decode(appCredentialInfo.getEncryptFormValues());
            connectionCredential.setConfig(JSONUtils.toMap(formValuesStr));
        } catch (Exception e) {
            throw AuthenticationException.buildDecryptException(this.getClass(), "form values");
        }

        return connectionCredential;
    }
}
