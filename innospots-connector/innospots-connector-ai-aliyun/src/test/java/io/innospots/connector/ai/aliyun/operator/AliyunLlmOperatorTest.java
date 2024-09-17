package io.innospots.connector.ai.aliyun.operator;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/17
 */
class AliyunLlmOperatorTest {

    public Map<String,Object> options(){
        Map<String,Object> om = new HashMap<>();
        om.put("temperature",1.0);
        om.put("maxTokens",1500);
        om.put("topP",0.8);
        om.put("resultFormat","message");
        return om;
    }

    @Test
    void executeStream() {
        AliyunLlmOperator llmOperator = new AliyunLlmOperator(System.getenv("API_KEY"),options());
        StringBuilder content = new StringBuilder();
        llmOperator.executeStream(build("qwen2-1.5b-instruct"))
                .doOnNext(b->{
                    Map<String,Object> m = (Map<String, Object>) b;
                    System.out.println(m);
                    content.append(m.get("content"));
                })
                .blockLast();
        System.out.println(content);
    }

    @Test
    void execute() {
        AliyunLlmOperator llmOperator = new AliyunLlmOperator(System.getenv("API_KEY"),options());
        DataBody dataBody = llmOperator.execute(build("qwen2-1.5b-instruct"));
        System.out.println(dataBody.getBody());
        System.out.println(dataBody.getMeta());
    }

    BaseRequest build(String model){
        BaseRequest request = new BaseRequest();
        request.setTargetName(model);
        Map<String,Object> m = new HashMap<>();
        m.put("content","人工智能的发展限制,输出格式要求: {\"background\": Object ,\"challenge\": Object,\"algorithm\": Object,\"technical_limitation\": Object,\"ethic\": Object,\"expectation\": Object}  ");
        m.put("role","user");
        request.setBody(m);
        return request;
    }
}