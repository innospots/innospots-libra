package io.innospots.connector.ai.aliyun;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/17
 */
@Slf4j
public class AliyunCallTest {
    public static void callWithMessage()
            throws NoApiKeyException, ApiException, InputRequiredException {
        Generation gen = new Generation();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content("开源软件商用限制?").build();
        QwenParam param =
                QwenParam.builder().model("qwen2-7b-instruct").messages(Arrays.asList(userMsg))
                        .resultFormat(QwenParam.ResultFormat.MESSAGE)
                        .maxTokens(100)
                        .topP(0.8)
                        .apiKey(System.getenv("API_KEY"))
                        .build();
        GenerationResult result = gen.call(param);
        System.out.println(result);
    }

    public static void main(String[] args){
        try {
            callWithMessage();
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            log.error(e.getMessage(),e);
        }
        System.exit(0);
    }

}
