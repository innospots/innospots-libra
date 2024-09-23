package io.innospots.connector.ai.aliyun.operator;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/21
 */
class AliTtsOperatorTest {

    @Test
    void execute() {
        HashMap<String,Object> options = new HashMap<>();
        AliTtsOperator ttsOperator = new AliTtsOperator(System.getenv("DASHSCOPE_API_KEY"),options);
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setTargetName("cosyvoice-v1");
        baseRequest.addQuery("voice","loongstella");
        baseRequest.setBody("语音合成，又称文本转语音（Text-to-Speech，TTS），是将文本转换为自然语音的技术。该技术基于机器学习算法，通过学习大量语音样本，掌握语言的韵律、语调和发音规则，从而在接收到文本输入时生成真人般自然的语音内容。");
        DataBody<ByteBuffer> dataBody = ttsOperator.execute(baseRequest);
        ByteBuffer byteBuffer = dataBody.getBody();
        try {
            File dir = new File("/tmp/audio");
            if(!dir.exists()){
                dir.mkdirs();
            }
            Path file = Path.of(dir.getPath(),"tts_test.mp3");
            Files.write(file,byteBuffer.array());
            System.out.println("out file: "+file.toFile().getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}