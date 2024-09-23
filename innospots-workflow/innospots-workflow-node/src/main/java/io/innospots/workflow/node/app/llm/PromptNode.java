package io.innospots.workflow.node.app.llm;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.PlaceholderUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
public class PromptNode extends BaseNodeExecutor {

    private static final String FILED_PROMPT = "prompt_template";

    private String promptTemplate;

    @Override
    protected void initialize() {
        promptTemplate = this.valueString(FILED_PROMPT);
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput executionOutput = this.buildOutput(nodeExecution);
        String content=null;
        if(CollectionUtils.isNotEmpty(nodeExecution.getInputs())){
            for (ExecutionInput input : nodeExecution.getInputs()) {
                for (Map<String, Object> inputItem : input.getData()) {
                    Map<String, String> pInput = new HashMap<>();
                    inputItem.forEach((k, v) -> pInput.put(k, v.toString()));
                    content = PlaceholderUtils.replacePlaceholders(promptTemplate, pInput);
                    addToOutput(executionOutput,content);
                }
            }
        }
        if(content == null){
            content = this.promptTemplate;
            addToOutput(executionOutput,content);
        }
    }

    private void addToOutput(ExecutionOutput executionOutput,String content){
        for (ParamField outputField : this.ni.getOutputFields()) {
            executionOutput.addResult(outputField.getCode(), content);
            break;
        }
    }
}
