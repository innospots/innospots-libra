package io.innospots.workflow.node.ai.aliyun;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/24
 */
@Slf4j
public class AliTtsNode extends AliAiBaseNode<String, SpeechSynthesisParam> {


    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput executionOutput = this.buildOutput(nodeExecution);
        SpeechSynthesisParam synthesisParam = null;
        try {
            synthesisParam = buildParam(null);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw ValidatorException.buildInvalidException(this.getClass(), e, e.getMessage());
        }
        log.info("ali tts param:{}", synthesisParam);
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(synthesisParam, null);
        int position = 1;
        for (ExecutionInput input : nodeExecution.getInputs()) {
            for (Map<String, Object> inputItem : input.getData()) {
                String message = buildMessage(inputItem);
                try {
                    log.info("start tts, input: {}", message);
                    ByteBuffer byteBuffer = synthesizer.call(message);
                    if (byteBuffer == null) {
                        throw ResourceException.buildCreateException(this.getClass(), "tts result is null,", this.nodeCode(), this.nodeName(), synthesisParam.toString());
                    }
                    ExecutionResource er = this.saveResourceToLocal(byteBuffer.array(), "tts_"+position+"_"+nodeKey()+".mp3",nodeExecution);
//                    ExecutionResource er = IExecutionContextOperator.buildExecutionResource(byteBuffer.array(), "tts_"+position+"_"+nodeKey()+".mp3", nodeExecution.getContextDataPath());
                    log.info("tts complete, temp file: {}", er.getLocalUri());
                    executionOutput.addResult(er.toMetaInfo());
                    log.info("tts resource meta:{}", er.toMetaInfo());
                    executionOutput.addResource(0, er);
                }catch (RuntimeException e){
                    throw e;
                } catch (Exception e) {
                    throw ResourceException.buildIOException(this.getClass(), e, e.getMessage());
                }
            }//end for input
        }//end for inputs
    }

    @Override
    protected SpeechSynthesisParam buildParam(Map<String, Object> items) {
        SpeechSynthesisParam param =
                SpeechSynthesisParam.builder()
                        .model(this.modelName)
                        .apiKey(apiKey)
                        .build();
        fillOptions(this.ni.getData(), param);
        log.info("start tts, param:{}", param);
        return param;
    }

    @Override
    protected String buildMessage(Map<String, Object> inputItem) {
        if (this.promptField != null) {
            return (String) inputItem.get(this.promptField.getCode());
        }
        return inputItem.toString();
    }
}
