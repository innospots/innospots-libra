package io.innospots.workflow.node.ai.zhipu;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.ai.LlmExecuteMode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/2
 */
@Slf4j
class ZhipuLlmNodeTest {


    @Test
    void test(){
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(1L,0);
        InnospotsIdGenerator.build("127.0.0.1", 8080);
        NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution("zhi_llm", flowExecution);
        ExecutionInput executionInput = new ExecutionInput();
        Map<String,Object> data = new HashMap<>();
        data.put("prompt_txt","自我介绍");
        executionInput.addInput(data);
        nodeExecution.addInput(executionInput);
        ZhipuLlmNode zhipuLlmNode = build();
        zhipuLlmNode.invoke(nodeExecution);
        for (ExecutionOutput output : nodeExecution.getOutputs()) {
            for (Map<String, Object> result : output.getResults()) {
                System.out.println(result);
            }
        }

    }

    ZhipuLlmNode build(){
        ZhipuLlmNode zhipuLlmNode = new ZhipuLlmNode();
        zhipuLlmNode.setApiKey(System.getenv("ZP_KEY"));
        zhipuLlmNode.setModelName("GLM-4-Flash");
        zhipuLlmNode.setExecuteMode(LlmExecuteMode.SYNC);
        NodeInstance ni = new NodeInstance();
        zhipuLlmNode.setNi(ni);
        zhipuLlmNode.setPromptField(new ParamField("prompt_txt","prompt_txt", FieldValueType.STRING));
        zhipuLlmNode.setMaxTokens(1024);
        zhipuLlmNode.setTopP(0.8);
        zhipuLlmNode.setTemperature(0.1);
        zhipuLlmNode. client = new ClientV4.Builder(System.getenv("ZP_KEY"))
                .enableTokenCache()
                .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                .build();

        return zhipuLlmNode;
    }

    @Test
    void testZhipu(){
        ClientV4 client = new ClientV4.Builder(System.getenv("ZP_KEY"))
                .enableTokenCache()
                .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
                .build();
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "ChatGLM和你哪个更强大");
        messages.add(chatMessage);
        String requestId = String.format("requestIdTemplate-%d", System.currentTimeMillis());


        HashMap<String, Object> extraJson = new HashMap<>();
        extraJson.put("temperature", 0.5);

        ChatMeta meta = new ChatMeta();
        meta.setUser_info("我是陆星辰，是一个男性，是一位知名导演，也是苏梦远的合作导演。我擅长拍摄音乐题材的电影。苏梦远对我的态度是尊敬的，并视我为良师益友。");
        meta.setBot_info("苏梦远，本名苏远心，是一位当红的国内女歌手及演员。在参加选秀节目后，凭借独特的嗓音及出众的舞台魅力迅速成名，进入娱乐圈。她外表美丽动人，但真正的魅力在于她的才华和勤奋。苏梦远是音乐学院毕业的优秀生，善于创作，拥有多首热门原创歌曲。除了音乐方面的成就，她还热衷于慈善事业，积极参加公益活动，用实际行动传递正能量。在工作中，她对待工作非常敬业，拍戏时总是全身心投入角色，赢得了业内人士的赞誉和粉丝的喜爱。虽然在娱乐圈，但她始终保持低调、谦逊的态度，深得同行尊重。在表达时，苏梦远喜欢使用“我们”和“一起”，强调团队精神。");
        meta.setBot_name("苏梦远");
        meta.setUser_name("陆星辰");

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("GLM-4-Flash")
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .temperature(0.5f)
//                .meta(meta)
//                .extraJson(extraJson)
                .build();
        log.info("request:{}",JSONUtils.toJsonString(chatCompletionRequest));
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        log.info("model output: {}", JSONUtils.toJsonString(invokeModelApiResp));
    }


}