package io.innospots.connector.ai.aliyun.minder;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import io.innospots.connector.core.credential.model.ConnectionCredential;
import io.innospots.connector.core.minder.BaseDataConnectionMinder;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.json.JSONUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/21
 */
@Slf4j
public class AliAiApiConnectorMinder extends BaseDataConnectionMinder {
    public static final String API_KEY = "api_key";

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public String schemaName() {
        return "ali-api-key";
    }

    @Override
    public <Operator extends IOperator> Operator buildOperator() {
        return null;
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        String apiKey = connectionCredential.v(API_KEY);
        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content("你是谁?").build();
            GenerationParam param =
                    GenerationParam.builder().model("qwen2-0.5b-instruct").messages(Arrays.asList(userMsg))
                            .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                            .maxTokens(20)
                            .topP(0.8)
                            .apiKey(apiKey)
                            .build();
            GenerationResult result = gen.call(param);
            log.info("test connect success:{} ", JSONUtils.toJsonString(result));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        return null;
    }

}
