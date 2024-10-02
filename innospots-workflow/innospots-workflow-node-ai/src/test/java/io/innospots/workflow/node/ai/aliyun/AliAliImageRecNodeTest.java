package io.innospots.workflow.node.ai.aliyun;

import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.OSSUtils;
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
import io.innospots.workflow.node.ai.LlmExecuteMode;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/26
 */
class AliAliImageRecNodeTest {


    AliImageRecNode build(){
        AliImageRecNode recNode = new AliImageRecNode();
        recNode.imageField = new ParamField("image","image", FieldValueType.STRING);
        recNode.setPromptField(new ParamField("text","text",FieldValueType.STRING));
        recNode.setExecuteMode(LlmExecuteMode.SYNC);
        recNode.setModelName("qwen-vl-plus");
        recNode.setApiKey(System.getenv("API_KEY"));
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

    @Test
    void test4() throws NoApiKeyException, IOException {
        String url = "https://i.pinimg.com/736x/37/e2/39/37e239d56d26c524646a4ffb49a07a79.jpg";
        UrlResource resource = new UrlResource(url);
        File f = new File("/tmp/out",resource.getFilename());
        Files.write(f.toPath(), resource.getContentAsByteArray());
        System.out.println(f.getAbsolutePath());
        System.out.println(System.getenv("DASHSCOPE_API_KEY"));
        String paths = OSSUtils.upload(ImageSynthesis.Models.WANX_V1,f.getAbsolutePath(), System.getenv("DASHSCOPE_API_KEY"));
        System.out.println(paths);
    }

}