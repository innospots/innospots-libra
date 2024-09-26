package io.innospots.workflow.node.ai.aliyun;

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
import io.innospots.base.exception.ResourceException;
import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/18
 */
@Slf4j
public class AliLlmAiNode extends AliAiBaseNode<Message, GenerationParam> {


    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput executionOutput = this.buildOutput(nodeExecution);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                Object result = processItem(item, nodeExecution);
                processOutput(nodeExecution, result, executionOutput);
            }
        }
    }

    @Override
    protected Object processItem(Map<String, Object> inputItem, NodeExecution nodeExecution) {
        Generation gen = new Generation();
        List<Map<String, Object>> outList = new ArrayList<>();
        GenerationResult gr = null;
        GenerationParam generationParam = null;
        StringBuilder content = new StringBuilder();
        switch (executeMode) {
            case STREAM:
                generationParam = generationParam(inputItem, true);
                try {
                    Flowable<GenerationResult> flowable = gen.streamCall(generationParam);
                    flowable
                            .doOnNext(result -> {
                                        if (result.getOutput() != null && result.getOutput().getChoices() != null) {
                                            List<GenerationOutput.Choice> choices = result.getOutput().getChoices();
                                            for (GenerationOutput.Choice choice : choices) {
                                                content.append(choice.getMessage().getContent());
                                            }
                                        }else if(result.getOutput()!=null){
                                            content.append(result.getOutput().getText());
                                        }
                                        flowLogger.item(nodeExecution.getFlowExecutionId(), BeanUtil.beanToMap(result, new HashMap<>(),CopyOptions.create().ignoreError().ignoreNullValue()));
                                        log.info("stream result:{}", result);
                                    }
                            )
                            .blockingSubscribe();
                } catch (NoApiKeyException | InputRequiredException e) {
                    log.error(e.getMessage(), e);
                    throw ResourceException.buildCreateException(AliLlmAiNode.class, e);
                }
                break;
            case SYNC:
                try {
                    generationParam = generationParam(inputItem, false);
                    gr = gen.call(generationParam);
                    log.debug("ll result:{}", JSONUtils.toJsonString(gr));
                    if (gr.getOutput().getText() != null) {
                        content.append(gr.getOutput().getText());
                    } else if (gr.getOutput().getChoices() != null) {
                        for (GenerationOutput.Choice choice : gr.getOutput().getChoices()) {
                            content.append(choice.getMessage().getContent());
                        }
                    }

                } catch (NoApiKeyException | InputRequiredException e) {
                    log.error(e.getMessage(), e);
                    throw ResourceException.buildCreateException(AliLlmAiNode.class, e);
                }
                break;
            default:
        }
        return content.toString();
    }

    @Override
    protected void processOutput(NodeExecution nodeExecution, Object result, ExecutionOutput nodeOutput) {
        if (result instanceof Map) {
            nodeOutput.addResult((Map<String, Object>) result);
        } else if (result instanceof Collection<?>) {
            nodeOutput.addResult((Collection<Map<String, Object>>) result);
        } else if (result instanceof String) {
            try {
                String r = (String) result;
                if (r.startsWith("{") && r.endsWith("}")) {
                    Map m = JSONUtils.toMap(r);
                    if (m != null) {
                        nodeOutput.addResult(m);
                    } else {
                        nodeOutput.addResult("response", r);
                    }
                } else if (r.startsWith("[") && r.endsWith("]")) {
                    Collection collection = JSONUtils.toList(r, Map.class);
                    if (CollectionUtils.isNotEmpty(collection)) {
                        nodeOutput.addResult(collection);
                    } else {
                        nodeOutput.addResult("response", r);
                    }
                } else {
                    nodeOutput.addResult("response", result);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                nodeOutput.addResult("response", result);
            }
        }
    }

    @Override
    protected GenerationParam buildParam(Map<String, Object> inputItem) {
        return generationParam(inputItem, false);
    }

    private GenerationParam generationParam(Map<String, Object> inputItem, boolean stream) {
        List<Message> messages = buildMessages(inputItem);
        GenerationParam generationParam = GenerationParam.builder()
                .model(modelName)
                .messages(messages)
                .apiKey(apiKey)
                .incrementalOutput(stream)
                .build();
        fillOptions(inputItem, generationParam);
        log.info("llm param:{}", generationParam);
        return generationParam;
    }


    protected List<Message> buildMessages(Map<String, Object> inputItem) {
        Object prompt = inputItem.get(this.promptField.getCode());
        List<Message> messages = new ArrayList<>();
        if (prompt instanceof List) {
            List<Map> bodyList = (List<Map>) prompt;
            for (Map map : bodyList) {
                fillMessage(messages, map);
            }
        } else if (prompt instanceof Map) {
            fillMessage(messages, (Map<String, Object>) prompt);
        } else if (prompt instanceof String) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", prompt);
            item.put("role", "user");
            fillMessage(messages, item);
        }

        return messages;
    }

    private void fillMessage(List<Message> messages, Map<String, Object> item) {
        Message message = buildMessage(item);
        if (message != null) {
            messages.add(message);
        } else {
            log.warn("message not be built,{}", item);
        }
    }

    @Override
    protected Message buildMessage(Map<String, Object> item) {
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
        message = Message.builder()
                .content(String.valueOf(content))
                .role(role)
                .build();
        return message;
    }

}
