package io.innospots.connector.ai.ollama.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}