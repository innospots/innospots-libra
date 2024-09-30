package io.innospots.workflow.node.ai.aliyun;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.logger.FlowLoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/30
 */
class AliImageGenNodeTest {


    AliImageGenNode build() {
        AliImageGenNode genNode = new AliImageGenNode();
        genNode.generateMode = AliImageGenNode.GenerateImageMode.similarImage;
        genNode.refImageField = new ParamField("ref", "ref", FieldValueType.STRING);
        genNode.promptField = new ParamField("text", "text", FieldValueType.STRING);
        genNode.executeMode = LlmExecuteMode.SYNC;
        genNode.apiKey = System.getenv("DASHSCOPE_API_KEY");
        InnospotsIdGenerator.build("127.0.0.1", 8080);
        NodeInstance ni = new NodeInstance();
        genNode.setNi(ni);
        genNode.setFlowLogger(FlowLoggerFactory.getLogger());

        return genNode;
    }

    @Test
    void test5() {
        String refImage = "https://2c.zol-img.com.cn/product/67/472/ceW46eRbRE.jpg";
        AliImageGenNode genNode = build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        Map<String, Object> item = new HashMap<>();
        item.put("text", "相似的图片");
        item.put("ref", refImage);
        NodeExecution nodeExecution = buildNodeExecution(item);
        genNode.invoke(nodeExecution);
        System.out.println(nodeExecution);
    }

    NodeExecution buildNodeExecution(Map<String, Object> data) {
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(1L, 0);
        NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution("image_rec", flowExecution);
        nodeExecution.setContextDataPath(new File(".execution_contexts/image_rec"));
        ExecutionInput executionInput = new ExecutionInput();
        executionInput.addInput(data);
        nodeExecution.addInput(executionInput);
        return nodeExecution;
    }

    @Test
    void testSkt() {
        String url = "https://huarong123.oss-cn-hangzhou.aliyuncs.com/image/%E6%B6%82%E9%B8%A6%E8%8D%89%E5%9B%BE.png";
        String taskId = createAsyncTask();
        System.out.println(taskId);
        waitAsyncTask(taskId);
    }


    public String createAsyncTask() {
        String prompt = "一棵参天大树";
        String sketchImageUrl = "https://help-static-aliyun-doc.aliyuncs.com/assets/img/zh-CN/6609471071/p743851.jpg";
        String model = "wanx-sketch-to-image-lite";
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .model(model)
                .prompt(prompt)
                .n(1)
                .size("768*768")
                .sketchImageUrl(sketchImageUrl)
                .style("<watercolor>")
                .build();

        String task = "image2image";
        ImageSynthesis imageSynthesis = new ImageSynthesis(task);
        ImageSynthesisResult result = null;
        try {
            result = imageSynthesis.asyncCall(param);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        String taskId = result.getOutput().getTaskId();
        System.out.println("taskId=" + taskId);
        return taskId;
    }

    public void waitAsyncTask(String taskId) {
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            // If you have set the DASHSCOPE_API_KEY in the system environment variable, the apiKey can be null.
            result = imageSynthesis.wait(taskId, null);
        } catch (ApiException | NoApiKeyException e) {
            throw new RuntimeException(e.getMessage());
        }

        System.out.println(JsonUtils.toJson(result.getOutput()));
        System.out.println(JsonUtils.toJson(result.getUsage()));
    }

    @Test
    void testPaint() {
        AliImageGenNode genNode = build();
        genNode.imageUrlField = new ParamField("image", "image", FieldValueType.STRING);
        genNode.styleIndex = 3;
        Map<String, Object> item = new HashMap<>();
        item.put("image", "https://public-vigen-video.oss-cn-shanghai.aliyuncs.com/public/dashscope/test.png");
        NodeExecution nodeExecution = buildNodeExecution(item);
        List<ExecutionResource> ll = genNode.repaint(item, nodeExecution);
        for (ExecutionResource executionResource : ll) {
            System.out.println(executionResource);
        }
    }

}