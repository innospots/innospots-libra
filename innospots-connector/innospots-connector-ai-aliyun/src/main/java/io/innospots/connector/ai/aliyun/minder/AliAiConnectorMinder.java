package io.innospots.connector.ai.aliyun.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaCatalog;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.connector.ai.aliyun.operator.AliyunAiOperator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/21
 */
public abstract class AliAiConnectorMinder extends BaseDataConnectionMinder {
    public static final String API_KEY = "api_key";
    public static final String MODEL_NAME = "model_name";

    protected String[] models;

    protected AliyunAiOperator<?,?,?> aliyunAiOperator;

    @Override
    public void close() {

    }

    @Override
    public String schemaName() {
        return "aliyun_schema";
    }

    @Override
    public IExecutionOperator buildOperator() {
        return aliyunAiOperator;
    }


    protected Map<String, Object> options() {
        return options(this.connectionCredential);
    }

    protected Map<String, Object> options(ConnectionCredential connectionCredential) {
        Map<String, Object> options = new HashMap<>();
        options.putAll(connectionCredential.getConfig());
        options.putAll(connectionCredential.getProps());
        return options;
    }


    @Override
    public SchemaRegistry schemaRegistryByCode(String registryCode) {
        return newRegistry(registryCode);
    }

    protected SchemaRegistry newRegistry(String modelName){
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        schemaRegistry.setCode(modelName);
        schemaRegistry.setRegistryId(modelName);
        schemaRegistry.setName(modelName);
        schemaRegistry.setConnectorName("API");
        fillRegistry(schemaRegistry,modelName);
        return schemaRegistry;
    }

    @Override
    public SchemaRegistry schemaRegistryById(String registryId) {
        return this.schemaRegistryByCode(registryId);
    }

    @Override
    public List<SchemaCatalog> schemaCatalogs() {
        return Arrays.stream(models).map(model ->
                (SchemaCatalog) schemaRegistryByCode(model))
                .collect(Collectors.toList());
    }

    private void fillRegistry(SchemaCatalog schemaRegistry, String model) {
        String[] splits = model.split(",");
        String code = splits[0];
        String name = splits[0];
        if(splits.length>1){
            name = splits[1];
        }
        schemaRegistry.setCode(code);
        schemaRegistry.setName(name);
        schemaRegistry.setConnectorName("API");
    }

    @Override
    public List<SchemaRegistry> schemaRegistries(boolean includeField) {
        return Arrays.stream(models).map(this::schemaRegistryByCode)
                .collect(Collectors.toList());
    }

    protected BaseRequest<?> testRequest(ConnectionCredential connectionCredential, String model, Object input, Map<String,Object> query){
        BaseRequest baseRequest = new BaseRequest<>();
        String modelName = connectionCredential.v(MODEL_NAME, model);
        baseRequest.setTargetName(modelName);
        if(query!=null){
            baseRequest.setQuery(query);
        }
        baseRequest.setBody(input);

        return baseRequest;
    }
}
