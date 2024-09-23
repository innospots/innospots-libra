package io.innospots.connector.ai.aliyun.operator;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.json.JSONUtils;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/17
 */
@Slf4j
public class AliyunLlmOperator extends AliyunAiOperator<GenerationParam, List<Message>, GenerationResult> {


    public AliyunLlmOperator(String apiKey, Map<String, Object> options) {
        super(apiKey,options);
    }

    @Override
    public Flux<?> executeStream(BaseRequest<?> itemRequest) {
        Generation gen = new Generation();
        GenerationParam generationParam = generationParam(itemRequest, true);
        Flux flux = null;
        try {
            Flowable<GenerationResult> flowable = gen.streamCall(generationParam);
            flux = Flux.from(flowable)
                    .map(result -> result.getOutput().getChoices())
                    .flatMap(Flux::fromIterable)
                    .map(choice -> BeanUtil.beanToMap(choice.getMessage()));
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error(e.getMessage(), e);
        }
        return flux;
    }

    @Override
    public <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        DataBody dataBody = new DataBody<>();
        GenerationResult gr = invoke(itemRequest);
        if (log.isDebugEnabled()) {
            log.debug("generation result:{}", JSONUtils.toJsonString(gr));
        }
        dataBody.setMeta(BeanUtil.beanToMap(gr.getUsage()));
        String content = gr.getOutput()
                .getChoices().stream().map(GenerationOutput.Choice::getMessage)
                .map(Message::getContent)
                .collect(Collectors.joining("||"));
        dataBody.setBody(content);
        dataBody.end();
        return dataBody;
    }

    @Override
    protected GenerationResult invoke(BaseRequest itemRequest) {
        Generation gen = new Generation();
        GenerationResult gr = null;
        GenerationParam generationParam = generationParam(itemRequest, false);
        try {
            gr = gen.call(generationParam);
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error(e.getMessage(), e);
            throw ResourceException.buildCreateException(AliyunLlmOperator.class, e);
        }
        return gr;
    }

    @Override
    protected GenerationParam buildParam(BaseRequest<?> request) {
        return generationParam(request, false);
    }

    private GenerationParam generationParam(BaseRequest<?> itemRequest, boolean stream) {
        List<Message> messages = buildMessage(itemRequest);
        String modelName = itemRequest.getTargetName();
        if (modelName == null) {
            modelName = (String) options.get("model_name");
        }
        GenerationParam generationParam = GenerationParam.builder()
                .model(modelName)
                .messages(messages)
                .apiKey(apiKey)
                .incrementalOutput(stream)
                .build();
        fillOptions(generationParam, itemRequest);
        log.info("llm param:{}",generationParam);
        return generationParam;
    }


    @Override
    protected List<Message> buildMessage(BaseRequest<?> itemRequest) {
        List<Message> messages = new ArrayList<>();
        if (itemRequest.getBody() instanceof List) {
            Message message = null;
            List<Map> bodyList = (List<Map>) itemRequest.getBody();
            for (Map map : bodyList) {
                fillMessage(messages, map);
            }
        } else if (itemRequest.getBody() instanceof Map) {
            fillMessage(messages, (Map<String, Object>) itemRequest.getBody());
        } else if (itemRequest.getBody() instanceof String) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", itemRequest.getBody());
            item.put("role", "user");
            fillMessage(messages, item);
        }

        return messages;
    }

    private static void fillMessage(List<Message> messages, Map<String, Object> item) {
        Message message = buildMessage(item);
        if (message != null) {
            messages.add(message);
        } else {
            log.warn("message not be built,{}", item);
        }
    }

    private static Message buildMessage(Map<String, Object> item) {
        Message message = null;
        String role = (String) item.get("role");
        if (role == null) {
            role = Role.USER.getValue();
        }
        Object content = item.get("content");
        if (content instanceof String) {
        } else {
            content = JSONUtils.toJsonString(content);
        }
        if (Role.USER.getValue().equals(role)) {
            List<ExecutionResource> resources = (List<ExecutionResource>) item.get("resources");
            if (CollectionUtils.isNotEmpty(resources)) {
                log.warn("current llm not support multi media resource");
            }
        }
        message = Message.builder()
                .content(String.valueOf(content))
                .role(role)
                .build();
        return message;
    }

}
