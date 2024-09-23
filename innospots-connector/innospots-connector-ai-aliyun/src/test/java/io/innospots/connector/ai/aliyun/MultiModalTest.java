package io.innospots.connector.ai.aliyun;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
public class MultiModalTest {

    public static void callWithLocalFile(String localPath)
            throws ApiException, NoApiKeyException, UploadFileException {
        String filePath = "file://"+localPath;
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(Arrays.asList(new HashMap<String, Object>(){{put("image", filePath);}},
                        new HashMap<String, Object>(){{put("text", "识别图片文字，以 JSON 格式输出");}})).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model("qwen-vl-plus")
                .message(userMessage)
                .apiKey(System.getenv("API_KEY"))
                .build();
        MultiModalConversationResult result = conv.call(param);
        System.out.println(JsonUtils.toJson(result));
    }

    public static void main(String[] args) {
        try {
            callWithLocalFile("/tmp/out/fapiao.png");
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

}
