package io.innospots.connector.ai.ollama.operator;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.json.JSONUtils;
import io.innospots.connector.ai.ollama.prompt.RequestPromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/15
 */
@Slf4j
public class OllamaOperator implements IExecutionOperator {

    private OllamaApi ollamaApi;

    private Map<String, Object> options;


    public OllamaOperator(String serviceAddress, Map<String, Object> options) {
        this.options = options;
        if (serviceAddress == null) {
            this.ollamaApi = new OllamaApi();
            return;
        }
        if (serviceAddress.startsWith("http://")) {
            this.ollamaApi = new OllamaApi(serviceAddress);
        } else {
            this.ollamaApi = new OllamaApi("http://" + serviceAddress);
        }
    }

    @Override
    public <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        DataBody dataBody = new DataBody<>();
        OllamaChatModel chatModel = chatModel(itemRequest);
        ChatResponse chatResponse = chatModel.call(RequestPromptBuilder.build(itemRequest));
        Map<String, Object> resp = responseToMap(chatResponse);
        dataBody.setBody(resp);
        dataBody.end();
        return dataBody;
    }

    private Map<String,Object> responseToMap(ChatResponse chatResponse){
        AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("content", assistantMessage.getContent());
        resp.put("messageType", assistantMessage.getMessageType().getValue());
        if (assistantMessage.getMetadata() != null) {
            resp.put("metadata", assistantMessage.getMetadata());
        }
        if (assistantMessage.getToolCalls() != null) {
            resp.put("toolCalls", JSONUtils.toJsonString(assistantMessage.getToolCalls()));
        }
        return resp;
    }

    private OllamaChatModel chatModel(BaseRequest<?> request) {
        OllamaOptions ollamaOptions = buildOptions(request);
        return OllamaChatModel.builder().withOllamaApi(ollamaApi).withDefaultOptions(ollamaOptions).build();
    }


    private OllamaOptions buildOptions(BaseRequest<?> request) {
        OllamaOptions ollamaOptions = OllamaOptions.builder().build();
        if (options != null) {
            BeanUtil.fillBeanWithMap(options, ollamaOptions, CopyOptions.create().ignoreError().ignoreNullValue());
        }
        if (request.getTargetName() != null) {
            ollamaOptions.setModel(request.getTargetName());
        }
        if (request.getQuery() != null) {
            BeanUtil.fillBeanWithMap(request.getQuery(), ollamaOptions, CopyOptions.create().ignoreError().ignoreNullValue());
        }
        return ollamaOptions;
    }

    @Override
    public Flux<Map<String, Object>> executeStream(BaseRequest<?> itemRequest) {
        Prompt prompt = RequestPromptBuilder.build(itemRequest);
        ChatModel chatModel = chatModel(itemRequest);
        return chatModel.stream(prompt).map(this::responseToMap);
    }
}
