package io.innospots.workflow.node.ai.aliyun;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/23
 */
@Slf4j
public class AliImageRecNode extends AliAiBaseNode<MultiModalMessage,MultiModalConversationParam> {

    public static final String FILED_IMAGE = "image";

    protected ParamField imageField;


    @Override
    protected void initialize() {
        super.initialize();
        this.imageField = NodeInstanceUtils.buildParamField(this.ni, FILED_IMAGE);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
    }

    @Override
    protected Object processItem(Map<String, Object> item,NodeExecution nodeExecution) {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationParam param = buildParam(item);
        List<Map<String, Object>> ll = new ArrayList<>();
        try {
            log.info("start recognize image,{}",param);
            if(this.executeMode == LlmExecuteMode.SYNC){
                MultiModalConversationResult result = conv.call(param);
                log.info("rec result:{}",result);
                for (MultiModalConversationOutput.Choice choice : result.getOutput().getChoices()) {
                    ll.addAll(choice.getMessage().getContent());
                }
            }else{
                Flowable<MultiModalConversationResult> fluxStream = conv.streamCall(param);
                if (fluxStream == null) {
                    return null;
                }
                StringBuilder content = new StringBuilder();
                fluxStream.doOnNext(b -> {
                    MultiModalConversationOutput output = b.getOutput();
                    for (MultiModalConversationOutput.Choice choice : output.getChoices()) {
                        for (Map<String, Object> map : choice.getMessage().getContent()) {
                            //ll.add(map);
                            String txt = (String) map.get("text");
                            if(txt!=null){
                                content.append(txt);
                            }
                            log.info("reg image stream output:{}",map);
                            flowLogger.item(nodeExecution.getFlowExecutionId(),map);
                        }
                    }//end for
                }).blockingSubscribe();
                Map<String,Object> mm = new HashMap<>();
                mm.put("text",content.toString());
                ll.add(mm);
            }

        } catch (NoApiKeyException | UploadFileException e) {
            log.error(e.getMessage(), e);
            throw ResourceException.buildCreateException(AliImageRecNode.class, e);
        }

        if(ll.size() == 1){
            return ll.get(0);
        }
        return ll;
    }

    @Override
    protected MultiModalConversationParam buildParam(Map<String, Object> items) {
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model(modelName)
                .message(buildMessage(items))
                .apiKey(apiKey)
                .incrementalOutput(LlmExecuteMode.STREAM == executeMode)
                .build();
        //fillOptions(items, param);

        return param;
    }


    @Override
    protected MultiModalMessage buildMessage(Map<String, Object> inputItem) {
        Object image = inputItem.get(imageField.getCode());
        String text = (String) inputItem.get(promptField.getCode());
        List<Map<String, Object>> inputs = new ArrayList<>();
        if (text != null) {
            inputs.add(Collections.singletonMap("text", text));
        }
        if (image instanceof String) {
            HashMap<String, Object> map = new HashMap<>(1);
            map.put("image", image);
            inputs.add(map);
        } else if (image instanceof List) {
            for (Object item : (List) image) {
                HashMap<String, Object> map = new HashMap<>(1);
                map.put("image", item);
                inputs.add(map);
            }
        }
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(inputs).build();
        return userMessage;
    }


}
