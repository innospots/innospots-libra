package io.innospots.workflow.node.app.execute;

import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.field.ValueParamField;
import io.innospots.workflow.core.node.instance.NodeInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/20
 */
public class FieldExtendNode extends BaseAppNode {

    private static final String FIELD_REPLACE = "extend_fields";

    private static final String OUTPUT_RESTRICTED = "output_restricted";

    private List<ValueParamField> valueParamFields;

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        validFieldConfig(FIELD_REPLACE);
        List<Map<String, Object>> v = (List<Map<String, Object>>) nodeInstance.value(FIELD_REPLACE);
        valueParamFields = new ArrayList<>();
        for (Map<String, Object> field : v) {
            valueParamFields.add(JSONUtils.parseObject(field, ValueParamField.class));
        }
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                for (ValueParamField valueParamField : valueParamFields) {
                    item.put(valueParamField.getField().getCode(),valueParamField.replace(item));
                }
                nodeOutput.addResult(item);
            }//end executionInput
        }//end input
    }

}
