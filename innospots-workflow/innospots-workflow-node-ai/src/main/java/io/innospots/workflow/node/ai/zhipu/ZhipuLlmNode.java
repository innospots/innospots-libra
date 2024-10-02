package io.innospots.workflow.node.ai.zhipu;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.node.ai.AiBaseNode;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/2
 */
@Slf4j
public class ZhipuLlmNode extends AiBaseNode<List<ChatMessage>, ChatCompletionRequest> {

    protected ClientV4 client;
    private static final String requestIdTemplate = "innospots-%d";


    @Override
    protected void initialize() {
        super.initialize();
        client = new ClientV4.Builder(apiKey)
                .enableTokenCache()
                .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                .build();
        if (promptField == null) {
            throw ValidatorException.buildMissingException(ZhipuLlmNode.class, "prompt field is null");
        }
    }

    @Override
    protected Object processItem(Map<String, Object> item, NodeExecution nodeExecution) {
        ChatCompletionRequest chatCompletionRequest = buildParam(item, this.executeMode.isStream());
        StringBuilder content = new StringBuilder();
        ModelApiResponse apiResp = client.invokeModelApi(chatCompletionRequest);
        if (this.executeMode.isStream()) {
            if (apiResp.isSuccess()) {
                Flowable<ModelData> flowable = apiResp.getFlowable();
                flowable
                        .doOnNext(result -> {
                            if (result.getChoices() != null) {
                                List<Choice> choices = result.getChoices();
                                for (Choice choice : choices) {
                                    content.append(choice.getMessage().getContent());
                                }
                            }
                        }).blockingSubscribe();
            }
        } else {
            log.info("response:{}",JSONUtils.toJsonString(apiResp));
            if (apiResp.getData()!=null&&apiResp.getData().getChoices() != null) {
                for (Choice choice : apiResp.getData().getChoices()) {
                    content.append(choice.getMessage().getContent());
                }
            }
        }
        return content.toString();
    }


    @Override
    protected ExecutionResource processResource(ExecutionResource resource, NodeExecution nodeExecution) {
        return super.processResource(resource, nodeExecution);
    }

    @Override
    protected ChatCompletionRequest buildParam(Map<String, Object> items, boolean stream) {
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());
        ChatCompletionRequest param = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(buildMessage(items))
                .maxTokens(maxTokens)
                .stream(stream)
                .invokeMethod(Constants.invokeMethod)
                .requestId(requestId)
                .build();
        if(temperature!=null){
            param.setTemperature(temperature.floatValue());
        }
        if(topP!=null){
            param.setTopP(topP.floatValue());
        }
        log.info("zhipu request:{}", JSONUtils.toJsonString(param));
        return param;
    }

    @Override
    protected List<ChatMessage> buildMessage(Map<String, Object> inputItem) {
        Object inputs = inputItem.get(this.promptField.getCode());
        List<ChatMessage> messages = new ArrayList<>();
        if (inputs instanceof List) {
            List<String> list = (List<String>) inputs;
            for (String prompt : list) {
                ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt);
                messages.add(chatMessage);
            }
        } else {
            ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), inputs.toString());
            messages.add(chatMessage);
        }
        return messages;
    }
}
