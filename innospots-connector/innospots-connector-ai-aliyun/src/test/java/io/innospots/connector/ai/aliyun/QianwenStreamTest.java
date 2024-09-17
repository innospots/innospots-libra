package io.innospots.connector.ai.aliyun;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/17
 */
public class QianwenStreamTest {
    private static final Logger logger = LoggerFactory.getLogger(QianwenStreamTest.class);

    private static void handleGenerationResult(GenerationResult message, StringBuilder fullContent) {
        fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
        logger.info("Received message: {}", JsonUtils.toJson(message));
    }

    public static void streamCallWithMessage(Generation gen, Message userMsg)
            throws NoApiKeyException, ApiException, InputRequiredException {
        GenerationParam param = buildGenerationParam(userMsg);
        Flowable<GenerationResult> result = gen.streamCall(param);
        StringBuilder fullContent = new StringBuilder();
        result.doOnNext(msg->{
            List<GenerationOutput.Choice> choices = msg.getOutput().getChoices();
            System.out.println("size:"+choices.size());
            for (GenerationOutput.Choice choice : choices) {
                System.out.println(JsonUtils.toJson(choice.getMessage()));
            }
        }).blockingSubscribe();

//        result.blockingForEach(message -> handleGenerationResult(message, fullContent));

        logger.info("Full content: \n{}", fullContent.toString());
    }

    public static void streamCallWithCallback(Generation gen, Message userMsg)
            throws NoApiKeyException, ApiException, InputRequiredException, InterruptedException {
        GenerationParam param = buildGenerationParam(userMsg);
        Semaphore semaphore = new Semaphore(0);
        StringBuilder fullContent = new StringBuilder();

        gen.streamCall(param, new ResultCallback<GenerationResult>() {
            @Override
            public void onEvent(GenerationResult message) {
                handleGenerationResult(message, fullContent);
            }

            @Override
            public void onError(Exception err) {
                logger.error("Exception occurred: {}", err.getMessage());
                semaphore.release();
            }

            @Override
            public void onComplete() {
                logger.info("Completed");
                semaphore.release();
            }
        });

        semaphore.acquire();
        logger.info("Full content: \n{}", fullContent.toString());
    }

    private static GenerationParam buildGenerationParam(Message userMsg) {
        return GenerationParam.builder()
                .model("qwen2-72b-instruct")
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .apiKey(System.getenv("API_KEY"))
                .incrementalOutput(true)
                .build();
    }

    public static void main(String[] args) {
        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content("如何做西红柿炖牛腩？").build();

            streamCallWithMessage(gen, userMsg);
            System.out.println("quit!!");
            System.exit(1);
            //streamCallWithCallback(gen, userMsg);
        } catch (Exception e) {
            logger.error("An exception occurred: {}", e.getMessage());
        }
    }
}
