package io.innospots.connector.ai.ollama.operator;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.data.request.SimpleRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/16
 */
class OllamaOperatorTest {

    @Test
    void test(){
        OllamaOperator ollamaOperator = new OllamaOperator("http://localhost:11434", new HashMap<>());
        BaseRequest request = new BaseRequest();
        request.setTargetName("qwen2:7b");
        request.addQuery("format","json");
        Map<String,Object> m = new HashMap<>();
        m.put("content","人工智能的发展限制,输出格式要求: {\"background\": Object ,\"challenge\": Object,\"algorithm\": Object,\"technical_limitation\": Object,\"ethic\": Object,\"expectation\": Object}  ");
        m.put("role","user");
        request.setBody(m);
        DataBody<Map<String,Object>> body = ollamaOperator.execute(request);
        Object content = body.getBody().get("content");
        System.out.println(content.getClass());
        System.out.println(content);
        //System.out.println(body);
    }

    @Test
    void testStream(){
        OllamaOperator ollamaOperator = new OllamaOperator("http://localhost:11434", new HashMap<>());
        BaseRequest request = new BaseRequest();
        request.setTargetName("qwen2:7b");
        request.addQuery("format","json");
        Map<String,Object> m = new HashMap<>();
        m.put("content","人工智能的发展限制,输出格式要求: {\"background\": Object ,\"challenge\": Object,\"algorithm\": Object,\"technical_limitation\": Object,\"ethic\": Object,\"expectation\": Object}  ");
        m.put("role","user");
        request.setBody(m);
        Flux body = ollamaOperator.executeStream(request);
        body.doOnNext(s->{
            System.out.println(s);
        }).doFinally(signalType -> System.out.println("finish"))
                .blockLast();

    }

}