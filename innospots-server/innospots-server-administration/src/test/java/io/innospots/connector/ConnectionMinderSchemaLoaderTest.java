package io.innospots.connector;

import io.innospots.base.connector.schema.config.ConnectionMinderSchema;
import io.innospots.base.connector.schema.config.ConnectionMinderSchemaLoader;
import org.junit.jupiter.api.Test;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/3/16
 */
class ConnectionMinderSchemaLoaderTest {

    @Test
    void reload() {

        for (ConnectionMinderSchema schema : ConnectionMinderSchemaLoader.connectionMinderSchemas()) {
            System.out.println(schema);
        }
    }
}