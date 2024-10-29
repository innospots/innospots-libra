package io.innospots.connector.core.schema.reader;

import io.innospots.connector.core.schema.model.SchemaRegistry;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/30
 */
public class SingleSchemaRegistryReader implements ISchemaRegistryReader{

    private SchemaRegistry schemaRegistry;

    public SingleSchemaRegistryReader(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    @Override
    public List<SchemaRegistry> listSchemaRegistries(String credentialKey, boolean includeField) {
        return List.of(schemaRegistry);
    }

    @Override
    public SchemaRegistry getSchemaRegistryByCode(String registryCode) {
        return schemaRegistry;
    }

    @Override
    public SchemaRegistry getSchemaRegistryById(String registryId) {
        return schemaRegistry;
    }
}
