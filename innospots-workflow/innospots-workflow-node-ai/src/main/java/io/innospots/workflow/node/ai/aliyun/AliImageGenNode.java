package io.innospots.workflow.node.ai.aliyun;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/23
 */
@Slf4j
public class AliImageGenNode extends AliAiBaseNode<String, ImageSynthesisParam> {

    private GenerateImageMode generateMode;

    private ParamField refImageField;

    private ParamField sketchImageField;

    private ParamField templateImagUrlField;

    private ParamField faceImageUrlField;

    private ParamField imageUrlField;
    private ParamField titleField;
    private ParamField subTitleField;
    private ParamField bodyTextField;
    private ParamField promptZhField;

    private Integer styleIndex;

    @Override
    protected void initialize() {
        super.initialize();
        styleIndex = this.valueInteger("style_index");
        generateMode = GenerateImageMode.valueOf(this.valueString("generate_image_mode"));
        refImageField = NodeInstanceUtils.buildParamField(this.ni, "refImage");
        sketchImageField = NodeInstanceUtils.buildParamField(this.ni, "sketchImageUrl");
        templateImagUrlField = NodeInstanceUtils.buildParamField(this.ni, "template_image_url");
        faceImageUrlField = NodeInstanceUtils.buildParamField(this.ni, "face_image_url");
        imageUrlField = NodeInstanceUtils.buildParamField(this.ni, "image_url");
        titleField = NodeInstanceUtils.buildParamField(this.ni, "title");
        subTitleField = NodeInstanceUtils.buildParamField(this.ni, "sub_title");
        bodyTextField = NodeInstanceUtils.buildParamField(this.ni, "body_text");
        promptZhField = NodeInstanceUtils.buildParamField(this.ni, "prompt_text_zh");
    }

    @Override
    protected Object processItem(Map<String, Object> item, NodeExecution nodeExecution) {
        Object er = null;
        switch (generateMode){
            case txt2image -> er = text2Image(item);
            case similarImage -> er = similarImage(item);
            case graffiti -> er = graffiti(item);
            case poster -> er = poster(item);
            case cosplay -> er = cosplay(item);
            case repaint -> er = repaint(item);
        }
        return er;
    }

    @Override
    protected void processOutput(NodeExecution nodeExecution, Object result, ExecutionOutput nodeOutput) {
        if(result instanceof ExecutionResource) {
            ExecutionResource er = (ExecutionResource) result;
            nodeOutput.addResource(er.getPosition(), er);
            result = er.toMetaInfo();
        }
        super.processOutput(nodeExecution, result, nodeOutput);
    }

    private Map repaint(Map<String,Object> item){
        RestClient restClient = buildClient("https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation");        Map<String,Object> data = new HashMap<>();
        data.put("model",GenerateImageMode.repaint.getModel());
        Map<String,Object> input = new HashMap<>();
        data.put("input",input);
        input.put("style_index",styleIndex);
        String imageUrl = imageUrlField!=null? (String) item.get(imageUrlField.getCode()) : null;
        input.put("image_url",imageUrl);
        return restClient.post().body(data).retrieve().toEntity(Map.class).getBody();
    }

    private Map<String,String> cosplay(Map<String,Object> item){
        RestClient restClient = buildClient("https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation");
        Map<String,Object> data = new HashMap<>();
        data.put("model",GenerateImageMode.cosplay.getModel());
        Map<String,Object> input = new HashMap<>();
        data.put("input",input);
        input.put("model_index",1);
        String faceImageUrl = faceImageUrlField!=null? (String) item.get(faceImageUrlField.getCode()) : null;
        input.put("face_image_url",faceImageUrl);
        String templateImageUrl = templateImagUrlField!=null? (String) item.get(templateImagUrlField.getCode()) : null;
        input.put("template_image_url",templateImageUrl);
        return restClient.post().body(data).retrieve().toEntity(Map.class).getBody();
    }

    private RestClient buildClient(String url){
        Consumer<HttpHeaders> defaultHeaders = (headers) -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.add("X-DashScope-Async","enable");
            headers.setBearerAuth(apiKey);
        };
        RestClient restClient = RestClient
                .builder()
                .baseUrl(url)
                .defaultHeaders(defaultHeaders)
                .build();
        return restClient;
    }

    private List<Map<String,String>> text2Image(Map<String,Object> item){
        List<Map<String,String>> rList = null;
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            ImageSynthesisParam param = buildParam(item);
            log.info("sync call image thesis, please wait a moment...");
            result = imageSynthesis.call(param);
            rList = result.getOutput().getResults();
        } catch (ApiException | NoApiKeyException e){
            log.error(e.getMessage(),e);
            throw ResourceException.buildCreateException(AliImageGenNode.class,e);
        }
        return rList;
    }

    private List<Map<String,String>> similarImage(Map<String,Object> item){
        return text2Image(item);
    }

    private List<Map<String,String>> graffiti(Map<String,Object> item){
        return text2Image(item);
    }

    private List<Map<String,String>> poster(Map<String,Object> item){
        return text2Image(item);
    }


    @Override
    protected ImageSynthesisParam buildParam(Map<String, Object> item) {
        String refImage = refImageField!=null? (String) item.get(refImageField.getCode()) : null;
        String sketchImage =sketchImageField!=null ? (String) item.get(sketchImageField.getCode()) : null;
        Map<String,Object> extraInputs = new HashMap<>();
        if(generateMode == GenerateImageMode.poster){
            extraInputs.put("generate_mode",this.valueString("generate_mode"));
            extraInputs.put("wh_ratios",this.valueString("wh_ratios"));
            extraInputs.put("lora_name",this.valueString("lora_name"));
            extraInputs.put("lora_weight",this.valueDouble("lora_weight"));
            extraInputs.put("ctrl_ratio",this.valueDouble("ctrl_ratio"));
            extraInputs.put("ctrl_step",this.valueDouble("ctrl_step"));
            extraInputs.put("generate_num",this.valueInteger("generate_num"));
            String title = titleField!=null? (String) item.get(titleField.getCode()) : null;
            String subTitle = subTitleField!=null? (String) item.get(subTitleField.getCode()) : null;
            String bodyText = bodyTextField!=null? (String) item.get(bodyTextField.getCode()) : null;
            String promptZh = promptZhField!=null? (String) item.get(promptZhField.getCode()) : null;
            if(title!=null){
                extraInputs.put("title",title);
            }
            if(subTitle!=null){
                extraInputs.put("sub_title",subTitle);
            }
            if(bodyText!=null){
                extraInputs.put("body_text",bodyText);
            }
            if(promptZh!=null){
                extraInputs.put("prompt_text_zh",promptZh);
            }
        }

        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .model(generateMode.getModel())
                        .apiKey(apiKey)
                        .prompt(buildMessage(item))
                        .refImage(refImage)
                        .sketchImageUrl(sketchImage)
                        .extraInputs(extraInputs)
                        .build();
        fillOptions(item,param);
        if (log.isDebugEnabled()) {
            log.debug("image synthesis param:{}", param);
        }
        return param;
    }

    @Override
    protected String buildMessage(Map<String, Object> inputItem) {
        return inputItem.get(promptField.getCode()).toString();
    }

    public enum GenerateImageMode {
        txt2image(ImageSynthesis.Models.WANX_V1),
        similarImage(ImageSynthesis.Models.WANX_V1),
        graffiti(ImageSynthesis.Models.WANX_SKETCH_TO_IMAGE_V1),
        poster("wanx-poster-generation-v1"),
        cosplay("wanx-style-cosplay-v1"),
        repaint("wanx-style-repaint-v1");

        private String model;


        GenerateImageMode(String model) {
            this.model = model;
        }

        public String getModel() {
            return model;
        }
    }

}
