package io.innospots.libra.kernel.module.credential.service;

import io.innospots.connector.core.credential.model.ConnectionCredential;
import io.innospots.connector.core.credential.model.CredentialInfo;
import io.innospots.connector.core.credential.model.CredentialType;
import io.innospots.connector.core.credential.operator.CredentialTypeOperator;
import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.store.CacheStoreManager;
import io.innospots.connector.core.credential.reader.ConnectionCredentialReader;
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
    private CredentialTypeOperator credentialTypeOperator;

    public Oauth2CallbackService(ConnectionCredentialReader connectionCredentialReader,
                                 CredentialTypeOperator credentialTypeOperator) {
        this.connectionCredentialReader = connectionCredentialReader;
        this.credentialTypeOperator = credentialTypeOperator;
    }

    public boolean authCallback(String credentialTypeCode, String code, String state) {
        if (StringUtils.isBlank(state)) {
            log.warn("oauth2 credential callback state can not be empty");
            throw ValidatorException.buildInvalidException("oauth2-credential", "oauth2 credential callback state can not be empty");
        }

        CredentialInfo credentialInfo = new CredentialInfo();
        credentialInfo.setCredentialTypeCode(credentialTypeCode);

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
