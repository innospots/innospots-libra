package io.innospots.connector.ai.ollama.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaCatalog;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.connector.ai.ollama.operator.OllamaOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/15
 */
@Slf4j
public class OllamaConnectorMinder extends BaseDataConnectionMinder {

    private OllamaOperator ollamaOperator;

    @Override
    public void open() {
        if(ollamaOperator!=null){
            return;
        }
        Map<String,Object> options = new HashMap<>();
        options.putAll(this.connectionCredential.getConfig());
        options.putAll(this.connectionCredential.getProps());
        ollamaOperator = new OllamaOperator(baseUrl(connectionCredential),
                options);
    }

    @Override
    public void close() {
    }

    private String baseUrl(ConnectionCredential credential){
        String address = credential.v("service_address");
        if(address == null){
            address = "http://localhost:11434";
        }else if(address.startsWith("http://")){
            return address;
        }else{
            address = "http://"+address;
        }
        return address;
    }

    @Override
    public SchemaRegistry schemaRegistryByCode(String registryCode) {
        Map<String,String> body = new HashMap<>();
        body.put("name",registryCode);
        ResponseEntity<Map> entity = restClient(connectionCredential)
                .post().uri("/api/show").body(body).retrieve().toEntity(Map.class);
        SchemaRegistry registry = new SchemaRegistry();
        if(entity.getStatusCode().is2xxSuccessful()){
            registry.setConfigs((Map<String, Object>) entity.getBody().get("model_info"));
        }
        registry.setName(registryCode);
        registry.setCode(registryCode);
        registry.setConnectorName("API");
        return registry;
    }

    @Override
    public SchemaRegistry schemaRegistryById(String registryId) {
        return this.schemaRegistryByCode(registryId);
    }

    @Override
    public List<SchemaRegistry> schemaRegistries(boolean includeField) {
        RestClient client = this.restClient(this.connectionCredential);
        ResponseEntity<Map> entity = client.get().uri("/api/tags").retrieve().toEntity(Map.class);
        List<SchemaRegistry> registries = new ArrayList<>();
        if(entity.getStatusCode().is2xxSuccessful()){
            Map<String,Object> body = entity.getBody();
            List<Map<String,Object>> models = (List<Map<String, Object>>) body.get("models");
            registries = models.stream().map(model -> {
                SchemaRegistry registry = new SchemaRegistry();
                String name = String.valueOf(model.get("name"));
                registry.setName(name);
                registry.setCode(name);
                registry.setConfigs((Map<String, Object>) model.get("details"));
                return registry;
            }).toList();
        }

        return registries;
    }

    @Override
    public List<SchemaCatalog> schemaCatalogs() {
        RestClient client = this.restClient(this.connectionCredential);
        ResponseEntity<Map> entity = client.get().uri("/api/tags").retrieve().toEntity(Map.class);
        List<SchemaCatalog> catalogs = new ArrayList<>();
        if(entity.getStatusCode().is2xxSuccessful()){
            Map<String,Object> body = entity.getBody();
            List<Map<String,Object>> models = (List<Map<String, Object>>) body.get("models");
            catalogs = models.stream().map(model -> {
                SchemaCatalog catalog = new SchemaCatalog();
                String name = String.valueOf(model.get("name"));
                catalog.setName(name);
                catalog.setDescription(String.valueOf(model.get("details")));
                catalog.setCode(name);
                return catalog;
            }).toList();
        }
        return catalogs;
    }

    @Override
    public String schemaName() {
        return "ollama_schema";
    }

    @Override
    public IExecutionOperator buildOperator() {
        return ollamaOperator;
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        ResponseEntity<String> entity = restClient(connectionCredential).get().uri("/api/tags").retrieve().toEntity(String.class);
        return entity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        Map<String,String> body = new HashMap<>();
        body.put("name",tableName);
        ResponseEntity<Map> entity = restClient(connectionCredential)
                .post().uri("/api/show").body(body).retrieve().toEntity(Map.class);
        return entity.getBody();
    }

    private RestClient restClient(ConnectionCredential connectionCredential){
        Consumer<HttpHeaders> defaultHeaders = (headers) -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        };
        return RestClient.builder().baseUrl(baseUrl(connectionCredential)).defaultHeaders(defaultHeaders).build();
    }
}
