package io.innospots.workflow.node.ai.aliyun;

import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.logger.FlowLoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/26
 */
class AliImageRecNodeTest {


    AliImageRecNode build(){
        AliImageRecNode recNode = new AliImageRecNode();
        recNode.imageField = new ParamField("image","image", FieldValueType.STRING);
        recNode.promptField = new ParamField("text","text",FieldValueType.STRING);
        recNode.executeMode =LlmExecuteMode.SYNC;
        recNode.modelName = "qwen-vl-plus";
        recNode.apiKey = System.getenv("API_KEY");
        InnospotsIdGenerator.build("127.0.0.1", 8080);

        NodeInstance ni = new NodeInstance();
        recNode.setNi(ni);
        recNode.setFlowLogger(FlowLoggerFactory.getLogger());
        return recNode;
    }

    @Test
    void test(){
        AliImageRecNode recNode = build();

        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(1L,0);
        NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution("image_rec", flowExecution);
        ExecutionInput executionInput = new ExecutionInput();
        Map<String,Object> data = new HashMap<>();
        data.put("text","识别图片内容，格式化输出");
        data.put("image","https://imgconvert.csdnimg.cn/aHR0cHM6Ly9haS5iZHN0YXRpYy5jb20vZmlsZS9EMjlBQ0Q0Q0ZBOEQ0MUQ3OUUwNjg2NENEMjVFQjM3OA?x-oss-process=image/format,png");
//        data.put("image","file:///tmp/imgs/test123.jpg");
//        data.put("image","https://ai.bdstatic.com/file/4969580B8691417A858A14B47C364441");
        executionInput.addInput(data);
        nodeExecution.addInput(executionInput);
        recNode.invoke(nodeExecution);
        for (ExecutionOutput output : nodeExecution.getOutputs()) {
            System.out.println(output.getResults());
        }
    }

    @Test
    void test2() throws NoApiKeyException, UploadFileException {
        AliImageRecNode recNode = build();
        MultiModalConversation conv = new MultiModalConversation();
        Map<String,Object> data = new HashMap<>();
        data.put("text","识别图片内容，格式化输出");
//        data.put("image","file:///tmp/imgs/test123.jpg");
        data.put("image","https://p1.ssl.qhimg.com/t019798fcdb75fcd33f.png");
        MultiModalMessage message = recNode.buildMessage(data);

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model("qwen-vl-plus")
                .apiKey(System.getenv("API_KEY"))
                .message(message)
                .build();
        System.out.println(param);
        MultiModalConversationResult result = conv.call(param);
        System.out.println(result);

    }

    @Test
    void test3(){
        File f = new File("/tmp/out/ffs12");
        HttpUtil.downloadFile("https://ai.bdstatic.com/file/4969580B8691417A858A14B47C364441",f);
        System.out.println(f.length());
        System.out.println(f.getPath());
        ExecutionResource er = ExecutionResource.buildResource(f,true, InnospotsWorkflowProperties.WORKFLOW_RESOURCES);
        System.out.println(er.toMetaInfo());
    }

}