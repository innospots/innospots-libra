package io.innospots.connector.ai.aliyun.operator;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.exception.ResourceException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
@Slf4j
public class AliImageRecOperator extends AliyunAiOperator<MultiModalConversationParam,MultiModalMessage,MultiModalConversationResult> {


    public AliImageRecOperator(String apiKey, Map<String, Object> options) {
        super(apiKey,options);
    }

    @Override
    public <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        DataBody dataBody = new DataBody();
        MultiModalConversationResult result = invoke(itemRequest);
        dataBody.setMeta(BeanUtil.beanToMap(result.getUsage()));
        List<Map<String, Object>> ll = new ArrayList<>();
        for (MultiModalConversationOutput.Choice choice : result.getOutput().getChoices()) {
            ll.addAll(choice.getMessage().getContent());
        }
        dataBody.setBody(ll);
        dataBody.end();
        return dataBody;
    }


    @Override
    protected MultiModalConversationResult invoke(BaseRequest request) {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationParam param = buildParam(request);
        MultiModalConversationResult result = null;
        try {
            log.info("start recognize image,{}",param);
            result = conv.call(param);
        } catch (NoApiKeyException | UploadFileException e) {
            log.error(e.getMessage(), e);
            throw ResourceException.buildCreateException(AliImageRecOperator.class, e);
        }
        return result;
    }

    @Override
    protected MultiModalConversationParam buildParam(BaseRequest<?> request) {
        String model = request.getTargetName();
        if (model == null) {
            model = (String) options.get("model_name");
        }
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model(model)
                .message(buildMessage(request))
                .apiKey(apiKey)
                .build();
        fillOptions(param, request);

        return param;
    }

    @Override
    protected MultiModalMessage buildMessage(BaseRequest<?> request) {
        Map<String,Object> body = (Map<String, Object>) request.getBody();
        Object image = body.get("image");
        String text = (String) body.get("text");
        List<Map<String, Object>> inputs = new ArrayList<>();
        if (text != null) {
            inputs.add(Collections.singletonMap("text",text));
        }
        if (image instanceof String) {
            HashMap<String,Object> map = new HashMap<>(1);
            map.put("image",image);
            inputs.add(map);
        } else if (image instanceof List) {
            for (Object item : (List) image) {
                HashMap<String,Object> map = new HashMap<>(1);
                map.put("image",item);
                inputs.add(map);
            }
        }
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(inputs).build();
        return userMessage;
    }


}
