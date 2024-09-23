package io.innospots.connector.ai.aliyun.minder;

import com.alibaba.dashscope.aigc.generation.GenerationParam;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.connector.ai.aliyun.operator.AliyunLlmOperator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/17
 */
@Slf4j
public class AliyunLlmConnectorMinder extends AliAiConnectorMinder {

    private static final String[] LLM_MODELS = {
            "qwen-coder-turbo",
            "qwen2.5-coder-7b-instruct",
            "qwen2.5-coder-1.5b-instruct",
            "qwen2-72b-instruct",
            "qwen2-57b-a14b-instruct",
            "qwen2-7b-instruct",
            "qwen2-1.5b-instruct",
            "qwen2-0.5b-instruct",
            "qwen-max",
            "qwen-max-longcontext",
            "qwen-plus",
            "qwen-turbo",
            "qwen-long",
            "qwen-math-plus",
            "qwen2-math-72b-instruct",
            "qwen2-math-7b-instruct",
            "qwen2-math-1.5b-instruct",
            "llama3.1-405b-instruct",
            "llama3.1-70b-instruct",
            "llama3.1-8b-instruct",
            "moonshot-v1-8k",
            "moonshot-v1-32k",
            "moonshot-v1-128k",
            "yi-large",
            "yi-medium",
            "yi-large-rag",
            "yi-large-turbo",
            "abab6.5s-chat",
            "abab6.5t-chat",
            "abab6.5g-chat"};


    @Override
    public void open() {
        models = LLM_MODELS;
        if (aliyunAiOperator == null) {
            aliyunAiOperator = new AliyunLlmOperator(this.connectionCredential.v(API_KEY), options());
        }
    }

    @Override
    public SchemaRegistry schemaRegistryByCode(String registryCode) {
        SchemaRegistry registry = super.schemaRegistryByCode(registryCode);
        List<SchemaField> schemaFields = new ArrayList<>();
        registry.setSchemaFields(schemaFields);
        schemaFields.add(new SchemaField().fill("role","role",
                FieldValueType.STRING, FieldScope.BODY));
        schemaFields.add(new SchemaField().fill("content","content",
                FieldValueType.STRING, FieldScope.BODY));
        schemaFields.add(new SchemaField().fill("incrementalOutput","incrementalOutput",
                FieldValueType.BOOLEAN, FieldScope.PARAM));
        schemaFields.add(new SchemaField().fill("topP","topP",
                FieldValueType.DOUBLE, FieldScope.PARAM));
        schemaFields.add(new SchemaField().fill("topK","topK",
                FieldValueType.INTEGER, FieldScope.PARAM));
        schemaFields.add(new SchemaField().fill("maxTokens","maxTokens",
                FieldValueType.DOUBLE, FieldScope.PARAM));
        schemaFields.add(new SchemaField().fill("temperature","temperature",
                FieldValueType.DOUBLE, FieldScope.PARAM));
        return registry;
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        String apiKey = connectionCredential.v(API_KEY);
        AliyunLlmOperator llmOperator = new AliyunLlmOperator(apiKey, options(connectionCredential));
        try {
            Map<String,Object> query = new HashMap<>();
            query.put("maxTokens","20");
            query.put("resultFormat",GenerationParam.ResultFormat.MESSAGE);
            query.put("topP",0.8);
            Map<String,Object> input = new HashMap<>();
            input.put("content","你叫什么名字");
            input.put("role","user");
            BaseRequest baseRequest =  testRequest(connectionCredential, "qwen2-0.5b-instruct",input,query);
            DataBody dataBody = llmOperator.execute(baseRequest);
            log.info("test connect success:{} ",dataBody.getBody());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        String apiKey = connectionCredential.v(API_KEY);
        String content = null;
        AliyunLlmOperator llmOperator = new AliyunLlmOperator(apiKey, options(connectionCredential));
        try {
            BaseRequest baseRequest = testRequest(connectionCredential, "qwen2-0.5b-instruct","你叫什么名字",null);
            DataBody dataBody = llmOperator.execute(baseRequest);
            log.info("execute result:{} ",dataBody.getBody());
            content = String.valueOf(dataBody.getBody());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return content;
    }

}
