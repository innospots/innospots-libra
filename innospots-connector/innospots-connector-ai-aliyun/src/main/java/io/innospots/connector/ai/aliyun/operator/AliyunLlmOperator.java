package io.innospots.connector.ai.aliyun.operator;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.BaseRequest;
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
public class AliyunLlmOperator implements IExecutionOperator {

    private String apiKey;

    private Map<String, Object> options;


    public AliyunLlmOperator(String apiKey) {
        this.apiKey = apiKey;
    }

    public AliyunLlmOperator(String apiKey, Map<String, Object> options) {
        this.apiKey = apiKey;
        this.options = options;
    }

    @Override
    public Flux<?> executeStream(BaseRequest<?> itemRequest) {
        Generation gen = new Generation();
        GenerationParam generationParam = generationParam(itemRequest);
        Flux flux = null;
        try {
            Flowable<GenerationResult> flowable = gen.streamCall(generationParam);
            flux = Flux.from(flowable)
                    .map(result-> result.getOutput().getChoices())
                    .flatMap(Flux::fromIterable)
                    .map(choice -> BeanUtil.beanToMap(choice.getMessage()));
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error(e.getMessage(),e);
        }
        return flux;
    }

    @Override
    public <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        DataBody dataBody = new DataBody<>();
        Generation gen = new Generation();
        GenerationParam generationParam = generationParam(itemRequest);
        try {
            GenerationResult gr =gen.call(generationParam);
            dataBody.setMeta(BeanUtil.beanToMap(gr.getUsage()));
            String content = gr.getOutput()
                    .getChoices().stream().map(GenerationOutput.Choice::getMessage)
                    .map(Message::getContent)
                    .collect(Collectors.joining("||"));
            dataBody.setBody(content);
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error(e.getMessage(),e);
        }
        dataBody.end();
        return dataBody;
    }


    private GenerationParam generationParam(BaseRequest<?> itemRequest) {
        List<Message> messages = buildMessages(itemRequest);
        String modelName = itemRequest.getTargetName();
        if (modelName == null) {
            modelName = (String) options.get("model_name");
        }
        GenerationParam generationParam = GenerationParam.builder()
                .model(modelName)
                .messages(messages)
                .apiKey(apiKey)
                .incrementalOutput(true)
                .build();
        fillOptions(generationParam, itemRequest);
        return generationParam;
    }

    private void fillOptions(GenerationParam generationParam, BaseRequest<?> request) {
        if (options != null) {
            BeanUtil.fillBeanWithMap(options, generationParam, CopyOptions.create().ignoreError().ignoreNullValue());
        }

        if (request.getQuery() != null) {
            BeanUtil.fillBeanWithMap(request.getQuery(), generationParam, CopyOptions.create().ignoreError().ignoreNullValue());
        }
    }

    public static List<Message> buildMessages(BaseRequest<?> itemRequest) {
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
        String content = (String) item.get("content");
        if (Role.USER.getValue().equals(role)) {
            List<ExecutionResource> resources = (List<ExecutionResource>) item.get("resources");
            if (CollectionUtils.isNotEmpty(resources)) {
                log.warn("current llm not support multi media resource");
            }
        }
        message = Message.builder()
                .content(content)
                .role(role)
                .build();
        return message;
    }

}
