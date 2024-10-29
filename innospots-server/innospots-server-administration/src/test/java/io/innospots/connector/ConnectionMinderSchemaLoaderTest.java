package io.innospots.connector;

import io.innospots.connector.core.meta.ConnectionMinderSchema;
import io.innospots.connector.core.meta.ConnectionMinderSchemaLoader;
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