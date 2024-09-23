package io.innospots.connector.ai.aliyun.operator;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
class AliImageRecOperatorTest {

    @Test
    void execute() {
        Map<String,Object> options = new HashMap<>();
        AliImageRecOperator imageRecOperator = new AliImageRecOperator(System.getenv("DASHSCOPE_API_KEY"), options);
        BaseRequest request = new BaseRequest();
        request.setTargetName("qwen-vl-plus");
        Map<String,Object> body = new HashMap<>();
        body.put("image","file:///tmp/imgs/test123.jpg");
        body.put("text","识别图像中的文字");
        request.setBody(body);
        DataBody dataBody = imageRecOperator.execute(request);
        System.out.println(dataBody.getMeta());
        System.out.println(dataBody.getBody());
    }
}