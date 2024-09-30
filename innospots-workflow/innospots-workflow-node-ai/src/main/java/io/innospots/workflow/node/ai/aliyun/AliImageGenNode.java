package io.innospots.workflow.node.ai.aliyun;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.OSSUtils;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    protected GenerateImageMode generateMode;

    protected ParamField refImageField;

    protected ParamField sketchImageField;

    protected ParamField templateImagUrlField;

    protected ParamField faceImageUrlField;

    protected ParamField imageUrlField;
    protected ParamField titleField;
    protected ParamField subTitleField;
    protected ParamField bodyTextField;
    protected ParamField promptZhField;

    protected Integer styleIndex;

    @Override
    protected void initialize() {
        super.initialize();
        styleIndex = this.valueInteger("style_index");
        generateMode = GenerateImageMode.valueOf(this.valueString("generate_image_mode"));
        refImageField = NodeInstanceUtils.buildParamField(this.ni, "ref_image");
        sketchImageField = NodeInstanceUtils.buildParamField(this.ni, "sketch_image_url");
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
        switch (generateMode) {
            case txt2image -> er = text2Image(item, nodeExecution);
            case similarImage -> er = similarImage(item, nodeExecution);
            case graffiti -> er = graffiti(item, nodeExecution);
            case poster -> er = poster(item, nodeExecution);
            case cosplay -> er = cosplay(item, nodeExecution);
            case repaint -> er = repaint(item, nodeExecution);
        }
        return er;
    }

    @Override
    protected void processOutput(NodeExecution nodeExecution, Object result, ExecutionOutput nodeOutput) {
        if (result instanceof ExecutionResource) {
            ExecutionResource er = (ExecutionResource) result;
            nodeOutput.addResource(er.getPosition(), er);
            result = er.toMetaInfo();
        } else if (result instanceof List && ((List<?>) result).get(0) instanceof ExecutionResource) {
            List<ExecutionResource> ers = (List<ExecutionResource>) result;
            List<Map<String, Object>> r = new ArrayList<>();
            for (ExecutionResource er : ers) {
                nodeOutput.addResource(er.getPosition(), er);
                r.add(er.toMetaInfo());
            }
            result = r;
        }
        super.processOutput(nodeExecution, result, nodeOutput);
    }

    protected List<ExecutionResource> repaint(Map<String, Object> item, NodeExecution nodeExecution) {
        Map<String, Object> data = new HashMap<>();
        data.put("model", GenerateImageMode.repaint.getModel());
        Map<String, Object> input = new HashMap<>();
        data.put("input", input);
        input.put("style_index", styleIndex);
        String imageUrl = imageUrlField != null ? (String) item.get(imageUrlField.getCode()) : null;
        if (imageUrl != null) {
            imageUrl = this.convertAndUploadImage(GenerateImageMode.repaint.getModel(), imageUrl);
        }else{
            throw ResourceException.buildNotExistException(AliImageRecNode.class, "image_url is null");
        }
        input.put("image_url", imageUrl);
        log.info("repaint post body:{}", data);

        boolean enableOss = imageUrl != null && imageUrl.startsWith("oss");
        RestClient restClient = buildClient("https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation", enableOss);
        Map<String, Object> res = restClient.post().body(data).retrieve().toEntity(Map.class).getBody();
        log.info("repaint result:{}", res);
        Map<String, String> output = (Map<String, String>) res.get("output");
        String taskId = output.get("task_id");
        if (taskId == null) {
            throw ResourceException.buildNotExistException(AliImageRecNode.class, "task_id is null");
        }
        return waitAsyncTask(taskId, nodeExecution);
    }

    public List<ExecutionResource> waitAsyncTask(String taskId, NodeExecution nodeExecution) {
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            result = imageSynthesis.wait(taskId, this.apiKey);
            log.info("task result:{}",result);
            return this.resolveResult(result, nodeExecution);
        } catch (ApiException | NoApiKeyException | MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw ResourceException.buildCreateException(AliImageGenNode.class, e);
        }
    }

    protected Map<String, String> cosplay(Map<String, Object> item, NodeExecution nodeExecution) {
        Map<String, Object> data = new HashMap<>();
        data.put("model", GenerateImageMode.cosplay.getModel());
        Map<String, Object> input = new HashMap<>();
        data.put("input", input);
        input.put("model_index", 1);
        String faceImageUrl = faceImageUrlField != null ? (String) item.get(faceImageUrlField.getCode()) : null;
        input.put("face_image_url", faceImageUrl);
        String templateImageUrl = templateImagUrlField != null ? (String) item.get(templateImagUrlField.getCode()) : null;
        input.put("template_image_url", templateImageUrl);
        if (faceImageUrl != null) {
            faceImageUrl = this.convertAndUploadImage(GenerateImageMode.cosplay.model, faceImageUrl);
        }
        if (templateImageUrl != null) {
            templateImageUrl = this.convertAndUploadImage(GenerateImageMode.cosplay.model, templateImageUrl);
        }
        boolean enableOss = (faceImageUrl != null && faceImageUrl.startsWith("oss"))
                || (templateImageUrl != null && templateImageUrl.startsWith("oss"));

        RestClient restClient = buildClient("https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation", enableOss);


        return restClient.post().body(data).retrieve().toEntity(Map.class).getBody();
    }

    private RestClient buildClient(String url, boolean enableOss) {
        Consumer<HttpHeaders> defaultHeaders = (headers) -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.add("X-DashScope-Async", "enable");
            if (enableOss) {
                headers.add("X-DashScope-OssResourceResolve", "enable");
            }
            headers.setBearerAuth(apiKey);
        };
        RestClient restClient = RestClient
                .builder()
                .baseUrl(url)
                .defaultHeaders(defaultHeaders)
                .build();
        return restClient;
    }

    private Object text2Image(Map<String, Object> item, NodeExecution nodeExecution) {
        List<ExecutionResource> rList = null;
        ImageSynthesis imageSynthesis = new ImageSynthesis(this.generateMode.task);
        ImageSynthesisResult result = null;
        try {
            ImageSynthesisParam param = buildParam(item);
            log.info("sync call image thesis, params:{}", param);
            result = imageSynthesis.call(param);
            if ("FAILED".equals(result.getOutput().getTaskStatus())) {
                throw ResourceException.buildCreateException(AliImageGenNode.class, "image synthesis failed, error message:" + result.getOutput());
            }
            log.info("image generate result:{}", result);
            rList = resolveResult(result, nodeExecution);
        } catch (ApiException | MalformedURLException | NoApiKeyException e) {
            log.error(e.getMessage());
            throw ResourceException.buildCreateException(AliImageGenNode.class, e);
        }
        return rList;
    }

    private List<ExecutionResource> resolveResult(ImageSynthesisResult result, NodeExecution nodeExecution) throws MalformedURLException {
        List<ExecutionResource> rList = new ArrayList<>();
        if (result.getOutput().getResults() != null) {
            int p = 0;
            for (Map<String, String> images : result.getOutput().getResults()) {
                if (images.containsKey("url")) {
                    String url = images.get("url");
                    ExecutionResource er = this.saveResourceToLocal(url, "png", nodeExecution);
                    er.setPosition(p++);
                    rList.add(er);

                }
            }
        }
        return rList;
    }

    private Object similarImage(Map<String, Object> item, NodeExecution nodeExecution) {
        return text2Image(item, nodeExecution);
    }

    private Object graffiti(Map<String, Object> item, NodeExecution nodeExecution) {
        return text2Image(item, nodeExecution);
    }

    private Object poster(Map<String, Object> item, NodeExecution nodeExecution) {
        return text2Image(item, nodeExecution);
    }


    @Override
    protected ImageSynthesisParam buildParam(Map<String, Object> item) {
        String refImage = refImageField != null ? (String) item.get(refImageField.getCode()) : null;
        String sketchImage = sketchImageField != null ? (String) item.get(sketchImageField.getCode()) : null;
        Map<String, Object> extraInputs = new HashMap<>();
        if (generateMode == GenerateImageMode.poster) {
            extraInputs.put("generate_mode", this.valueString("generate_mode"));
            extraInputs.put("wh_ratios", this.valueString("wh_ratios"));
            extraInputs.put("lora_name", this.valueString("lora_name"));
            extraInputs.put("lora_weight", this.valueDouble("lora_weight"));
            extraInputs.put("ctrl_ratio", this.valueDouble("ctrl_ratio"));
            extraInputs.put("ctrl_step", this.valueDouble("ctrl_step"));
            extraInputs.put("generate_num", this.valueInteger("generate_num"));
            String title = titleField != null ? (String) item.get(titleField.getCode()) : null;
            String subTitle = subTitleField != null ? (String) item.get(subTitleField.getCode()) : null;
            String bodyText = bodyTextField != null ? (String) item.get(bodyTextField.getCode()) : null;
            String promptZh = promptZhField != null ? (String) item.get(promptZhField.getCode()) : null;
            if (title != null) {
                extraInputs.put("title", title);
            }
            if (subTitle != null) {
                extraInputs.put("sub_title", subTitle);
            }
            if (bodyText != null) {
                extraInputs.put("body_text", bodyText);
            }
            if (promptZh != null) {
                extraInputs.put("prompt_text_zh", promptZh);
            }
        }
        if (refImage != null) {
            refImage = this.convertAndUploadImage(generateMode.getModel(), refImage);
        }
        if (sketchImage != null) {
            sketchImage = this.convertAndUploadImage(generateMode.getModel(), sketchImage);
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

        if ((refImage != null && refImage.startsWith("oss")) ||
                (sketchImage != null && sketchImage.startsWith("oss"))) {
            param.putHeader("X-DashScope-OssResourceResolve", "enable");
            log.info("set param header:{}", param.getHeaders());
        }
        fillOptions(item, param);
        if (log.isDebugEnabled()) {
            log.debug("image synthesis param:{}", param);
        }
        return param;
    }

    private String convertAndUploadImage(String model, String urlImage) {
        if (!urlImage.startsWith("http")) {
            return urlImage;
        }
        try {
            UrlResource resource = new UrlResource(urlImage);
            Path outPath = Files.createTempDirectory("ali_gen");
            File outFile = new File(outPath.toFile(), resource.getFilename());
            Files.write(outFile.toPath(), resource.getContentAsByteArray());
            log.info("write image temp file:{}", outFile.getAbsolutePath());
            urlImage = OSSUtils.upload(model, outFile.getAbsolutePath(), this.apiKey);
            log.info("upload to oos:{}", urlImage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return urlImage;
    }

    @Override
    protected String buildMessage(Map<String, Object> inputItem) {
        Object v = inputItem.get(promptField.getCode());
        if (v != null) {
            return v.toString();
        } else {
            throw ValidatorException.buildMissingException(this.getClass(), "prompt field is empty.");
        }
    }

    public enum GenerateImageMode {
        txt2image(ImageSynthesis.Models.WANX_V1, "text2image"),
        similarImage(ImageSynthesis.Models.WANX_V1, "text2image"),
        graffiti("wanx-sketch-to-image-lite", "image2image"),
        poster("wanx-poster-generation-v1", "text2image"),
        cosplay("wanx-style-cosplay-v1", ""),
        repaint("wanx-style-repaint-v1", "");

        private String model;

        private String task;


        GenerateImageMode(String model, String task) {
            this.model = model;
            this.task = task;
        }

        public String getModel() {
            return model;
        }
    }

}
