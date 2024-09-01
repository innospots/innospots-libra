package io.innospots.base.connector.credential.buidler;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.meta.ConnectionMinderSchema;
import io.innospots.base.connector.meta.ConnectionMinderSchemaLoader;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import org.slf4j.Logger;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/30
 */
public interface IRegistryCredentialBuilder {

    Logger log = org.slf4j.LoggerFactory.getLogger(IRegistryCredentialBuilder.class);

    default ConnectionCredential buildBySchemaRegistry(SchemaRegistry schemaRegistry) {
        if (schemaRegistry.getConnectorName() == null) {
            return null;
        }
        ConnectionCredential credential = new ConnectionCredential();
        credential.setAuthOption(schemaRegistry.getAuthOption());
        credential.setConnectorName(schemaRegistry.getConnectorName());
        ConnectionMinderSchema minderSchema = ConnectionMinderSchemaLoader.getConnectionMinderSchema(schemaRegistry.getConnectorName());
        credential.setCredentialTypeCode(minderSchema.getConnectType().name());
        credential.setConfig(schemaRegistry.getConfigs());
        if(schemaRegistry.getCredentialKey() == null){
            credential.setCredentialKey(schemaRegistry.getRegistryType() + "_" + schemaRegistry.getRegistryId());
        }else{
            log.warn("schema registry has credentialKey, ConnectionCredential should be load by IConnectionCredentialReader, credentialKey:{}",schemaRegistry.getCredentialKey());
            credential.setCredentialKey(schemaRegistry.getCredentialKey());
        }
        return credential;
    }
}
