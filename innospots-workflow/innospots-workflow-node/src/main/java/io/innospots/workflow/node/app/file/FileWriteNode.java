package io.innospots.workflow.node.app.file;

import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
@Slf4j
public class FileWriteNode extends BaseNodeExecutor {

    private static final String FIELD_CONTENT = "content_fields";
    private static final String FIELD_FILE_NAME = "file_name";

    private List<NodeParamField> contentFields;

    private String fileName;

    @Override
    protected void initialize() {
        this.fileName = this.validString(FIELD_FILE_NAME);
        contentFields = NodeInstanceUtils.buildParamFields(ni, FIELD_CONTENT);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = this.buildOutput(nodeExecution);
        StringBuilder content = new StringBuilder();
        for (ExecutionInput input : nodeExecution.getInputs()) {
            for (Map<String, Object> inputItem : input.getData()) {
                if(CollectionUtils.isNotEmpty(contentFields)){
                    for (NodeParamField contentField : contentFields) {
                        content.append(inputItem.get(contentField.getCode())).append("\n");
                    }
                }else{
                    inputItem.forEach((k,v)->content.append(v).append("\n"));
                }
            }
        }//end for

        if(content.isEmpty()){
            return;
        }
        String fileN = fileName == null? this.nodeKey() : fileName;
        ExecutionResource er = this.saveResourceToLocal(content.toString().getBytes(),fileN, nodeExecution);
        nodeOutput.addResource(0, er);
    }
}
