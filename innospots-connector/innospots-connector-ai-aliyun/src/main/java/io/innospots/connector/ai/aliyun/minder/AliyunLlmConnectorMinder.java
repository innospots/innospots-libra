package io.innospots.connector.ai.aliyun.minder;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.DollyParam;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaCatalog;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.operator.IOperator;
import io.innospots.connector.ai.aliyun.operator.AliyunLlmOperator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/17
 */
@Slf4j
public class AliyunLlmConnectorMinder extends BaseDataConnectionMinder {

    public static final String API_KEY = "api_key";
    public static final String MODEL_NAME = "model_name";
    private static final String[] MODELS = {
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


    private AliyunLlmOperator llmOperator;

    @Override
    public void open() {
        if(llmOperator == null){
            Map<String,Object> options = new HashMap<>();
            options.putAll(this.connectionCredential.getConfig());
            options.putAll(this.connectionCredential.getProps());
            llmOperator = new AliyunLlmOperator(this.connectionCredential.v(API_KEY),options);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String schemaName() {
        return "aliyun_schema";
    }

    @Override
    public IExecutionOperator buildOperator() {
        return llmOperator;
    }


    @Override
    public SchemaRegistry schemaRegistryByCode(String registryCode) {
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        schemaRegistry.setCode(registryCode);
        schemaRegistry.setRegistryId(registryCode);
        schemaRegistry.setName(registryCode);
        schemaRegistry.setConnectorName("API");
        return schemaRegistry;
    }

    @Override
    public SchemaRegistry schemaRegistryById(String registryId) {
        return this.schemaRegistryByCode(registryId);
    }

    @Override
    public List<SchemaCatalog> schemaCatalogs() {
        return Arrays.stream(MODELS).map(model -> {
            SchemaCatalog catalog = new SchemaCatalog();
            catalog.setCode(model);
            catalog.setName(model);
            catalog.setConnectorName("API");
            return catalog;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SchemaRegistry> schemaRegistries(boolean includeField) {
        return Arrays.stream(MODELS).map(model -> {
            SchemaRegistry schemaRegistry = new SchemaRegistry();
            schemaRegistry.setCode(model);
            schemaRegistry.setRegistryId(model);
            schemaRegistry.setName(model);
            schemaRegistry.setConnectorName("API");
            return schemaRegistry;
        }).collect(Collectors.toList());
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        String apiKey = connectionCredential.v(API_KEY);
        String modelName = connectionCredential.v(MODEL_NAME,"qwen2-0.5b-instruct");
        Generation gen = new Generation();
        GenerationParam gParam = generationParam(modelName,apiKey,"你叫什么名字");
        try {
            GenerationResult result = gen.call(gParam);
            log.info("generation result:{}", result.getOutput().getChoices().stream().map(Choice -> Choice.getMessage().getContent()).collect(Collectors.joining(",")));
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error(e.getMessage(),e);
            return false;
        }
        return true;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        String apiKey = connectionCredential.v(API_KEY);
        Generation gen = new Generation();
        GenerationParam gParam = generationParam(tableName,apiKey,"你叫什么名字");
        String content = null;
        try {
            GenerationResult result = gen.call(gParam);
            content = result.getOutput().getChoices().stream().map(Choice -> Choice.getMessage().getContent()).collect(Collectors.joining(","));
            log.info("generation result:{}", content);
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error(e.getMessage(),e);
        }
        return content;
    }


    private GenerationParam generationParam(String modelName,String apiKey, String content) {
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(content).build();
        return GenerationParam.builder()
                .model(modelName)
                .messages(Collections.singletonList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .maxTokens(20)
                .topP(0.8)
                .apiKey(apiKey)
                .build();
    }
}
