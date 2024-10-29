package io.innospots.connector.ai.ollama.minder;

import io.innospots.connector.core.credential.model.ConnectionCredential;
import io.innospots.connector.core.schema.model.SchemaRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/15
 */
class OllamaConnectorMinderTest {

    @Test
    void testConnect() {
        ConnectionCredential cc = new ConnectionCredential();
        cc.config("service_address", "http://localhost:11434");
        OllamaConnectorMinder minder = new OllamaConnectorMinder();
        Object object = minder.testConnect(cc);
        System.out.println(object);
    }

    @Test
    void fetchSample() {
        ConnectionCredential cc = new ConnectionCredential();
        cc.config("service_address", "http://localhost:11434");
        OllamaConnectorMinder minder = new OllamaConnectorMinder();
        Object object = minder.fetchSample(cc,"qwen2:7b");
        System.out.println(object);
    }

    @Test
    void testRegistry(){
        ConnectionCredential cc = new ConnectionCredential();
        cc.config("service_address", "http://localhost:11434");
        OllamaConnectorMinder minder = new OllamaConnectorMinder();
        minder.prepare(null, cc);
        List<SchemaRegistry> registryList = minder.schemaRegistries(true);
        for (SchemaRegistry schemaRegistry : registryList) {
            System.out.println(schemaRegistry.getName());
        }
    }
}