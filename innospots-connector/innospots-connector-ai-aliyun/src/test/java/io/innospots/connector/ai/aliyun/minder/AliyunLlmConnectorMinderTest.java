package io.innospots.connector.ai.aliyun.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import org.junit.jupiter.api.Test;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/17
 */
class AliyunLlmConnectorMinderTest {


    @Test
    void testConnect() {
        ConnectionCredential cc = new ConnectionCredential();
        cc.config("api_key", System.getenv("API_KEY"));
        cc.config("model_name", "qwen2-0.5b-instruct");
        AliyunLlmConnectorMinder minder = new AliyunLlmConnectorMinder();
        Object b = minder.testConnect(cc);
        System.out.println("results：" + b);
    }

    @Test
    void testSample(){
        ConnectionCredential cc = new ConnectionCredential();
        cc.config("api_key", System.getenv("API_KEY"));
        String modelName = "qwen2-7b-instruct";
        AliyunLlmConnectorMinder minder = new AliyunLlmConnectorMinder();
        Object r = minder.fetchSample(cc, modelName);
        System.out.println(r);
    }

}