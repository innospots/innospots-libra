package io.innospots.connector.ai.aliyun.operator;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/21
 */
@Slf4j
public class AliTtsOperator extends AliyunAiOperator<SpeechSynthesisParam,String, ByteBuffer>{


    public AliTtsOperator(String apiKey, Map<String, Object> options) {
        super(apiKey,options);
    }

    @Override
    public <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        DataBody dataBody = new DataBody<>();
        ByteBuffer audio = invoke(itemRequest);
        dataBody.setBody(audio);
        dataBody.end();
        return dataBody;
    }

    @Override
    protected ByteBuffer invoke(BaseRequest request) {
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(buildParam(request),null);
        return synthesizer.call(buildMessage(request));
    }

    @Override
    protected SpeechSynthesisParam buildParam(BaseRequest<?> request) {
        String model = request.getTargetName();
        if(model == null){
            model = (String) options.get("model_name");
        }
        String voice = (String) request.query("voice");
        if(voice == null){
            voice = (String) options.get("voice");
        }
        SpeechSynthesisParam param =
                SpeechSynthesisParam.builder()
                        .model(model)
                        .voice(voice)
                        .build();
        fillOptions(param, request);
        log.info("start tts, param:{}",param);
        return param;
    }

    @Override
    protected String buildMessage(BaseRequest<?> request) {
        return (String) request.getBody();
    }
}
