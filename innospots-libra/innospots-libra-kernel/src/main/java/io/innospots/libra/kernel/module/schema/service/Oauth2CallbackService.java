package io.innospots.libra.kernel.module.schema.service;

import io.innospots.base.connector.credential.ConnectionCredential;
import io.innospots.base.connector.credential.CredentialInfo;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.store.CacheStoreManager;
import io.innospots.libra.base.credential.reader.ConnectionCredentialReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/7/20
 */
@Slf4j
@Component
public class Oauth2CallbackService {

    private ConnectionCredentialReader connectionCredentialReader;

    public Oauth2CallbackService(ConnectionCredentialReader connectionCredentialReader) {
        this.connectionCredentialReader = connectionCredentialReader;
    }

    public boolean authCallback(String credentialType, String code, String state) {
        if (StringUtils.isBlank(state)) {
            log.warn("oauth2 credential callback state can not be empty");
            throw ValidatorException.buildInvalidException("oauth2-credential", "oauth2 credential callback state can not be empty");
        }
        CredentialInfo credentialInfo = new CredentialInfo();
        //credentialInfo.("oauth2-auth-api");
        credentialInfo.setConnectorName("Http");
        credentialInfo.setCredentialTypeCode(credentialType);
        String json = CacheStoreManager.get(state);
        if (StringUtils.isBlank(json)) {
            log.warn("oauth2 credential callback state invalid");
            return false;
        }
        Map<String, Object> formValues = null;
        ConnectionCredential connectionCredential = connectionCredentialReader.fillCredential(credentialInfo);
        connectionCredential.config("code", code);
        connectionCredential.config("state", state);
        if (json != null) {
            formValues = JSONUtils.toMap(json);
            connectionCredential.config(formValues);
            CacheStoreManager.remove(state);
        }

        Object result = DataConnectionMinderManager.testConnection(connectionCredential);
        log.debug("oauth authenticate:{}", result);
        boolean success = false;
        if (result instanceof Map && MapUtils.isNotEmpty((Map<?, ?>) result)) {
            success = true;
        }

        return success;
    }

}
