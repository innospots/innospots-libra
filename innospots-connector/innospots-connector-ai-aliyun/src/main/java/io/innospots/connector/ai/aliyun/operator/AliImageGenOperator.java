package io.innospots.connector.ai.aliyun.operator;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
@Slf4j
public class AliImageGenOperator extends AliyunAiOperator<ImageSynthesisParam, Map, ImageSynthesisResult> {


    public AliImageGenOperator(String apiKey, Map<String, Object> options) {
        super(apiKey,options);
    }

    @Override
    public <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        DataBody dataBody = new DataBody<>();
        ImageSynthesisResult result = invoke(itemRequest);
        dataBody.setMeta(BeanUtil.beanToMap(result.getUsage()));
        dataBody.setBody(result.getOutput());
        Map out = BeanUtils.toMap(result.getOutput());
        log.info("image generation result:{}",out);
        dataBody.setBody(out);
        dataBody.end();
        return dataBody;
    }


    @Override
    protected ImageSynthesisResult invoke(BaseRequest request) {
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            ImageSynthesisParam param = buildParam(request);
            log.info("sync call image thesis, please wait a moment...");
            result = imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e){
            log.error(e.getMessage(),e);
            throw ResourceException.buildCreateException(AliImageGenOperator.class,e);
        }
        return result;
    }

    @Override
    protected ImageSynthesisParam buildParam(BaseRequest<?> itemRequest){
        String model = itemRequest.getTargetName();
        if(model == null){
            model = (String) options.get("model_name");
        }
        Map<String,Object> body = (Map<String, Object>) itemRequest.getBody();
        String prompt = (String) body.get("prompt");
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .model(model)
                        .model(ImageSynthesis.Models.WANX_V1)
                        .apiKey(apiKey)
                        .prompt(prompt)
                        .n(1)
                        .size("1024*1024")
                        .build();
        fillOptions(param, itemRequest);
        if (log.isDebugEnabled()) {
            log.debug("image synthesis param:{}", param);
        }
        return param;
    }

    @Override
    protected Map buildMessage(BaseRequest<?> request) {
        return Map.of();
    }
}
