package io.innospots.connector.ai.ollama.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.operator.IOperator;
import io.innospots.connector.ai.ollama.operator.OllamaOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

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
        ollamaOperator = new OllamaOperator(baseUrl(connectionCredential));
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
