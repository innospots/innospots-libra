package io.innospots.workflow.node.app.execute;

import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.field.ExtendField;
import io.innospots.workflow.core.node.field.ValueParamField;
import io.innospots.workflow.core.node.instance.NodeInstance;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author Smars
 * @date 2023/8/20
 */
public class ValueBuildNode extends BaseAppNode {

    private static final String FIELD_REPLACE = "replace_fields";

    private static final String FIELD_EXTEND = "extend_fields";
    private static final String FIELD_ACTION = "field_action";

    private static final String OUTPUT_RESTRICTED = "output_restricted";

    private List<ValueParamField> valueParamFields;

    private List<ExtendField> extendFields;

    private FieldAction fieldAction;

    private boolean outputRestricted;

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        validFieldConfig(FIELD_ACTION);
        outputRestricted = nodeInstance.valueBoolean(OUTPUT_RESTRICTED);
        fieldAction = FieldAction.valueOf(this.valueString(FIELD_ACTION));
        if (fieldAction == FieldAction.FUNCTION) {
            List<Map<String, Object>> v = (List<Map<String, Object>>) nodeInstance.value(FIELD_EXTEND);
            extendFields = new ArrayList<>();
            for (Map<String, Object> field : v) {
                Object ff = field.get("field");
                if(ff == null || StringUtils.isEmpty(ff.toString())){
                    field.remove("field");
                }
                ExtendField extendField = JSONUtils.parseObject(field,ExtendField.class);
                extendField.initialize();
                extendFields.add(extendField);
            }
        } else {
            List<Map<String, Object>> v = (List<Map<String, Object>>) nodeInstance.value(FIELD_REPLACE);
            valueParamFields = new ArrayList<>();
            for (Map<String, Object> field : v) {
                Object ff = field.get("field");
                if(ff == null || StringUtils.isEmpty(ff.toString())){
                    field.remove("field");
                }
                ValueParamField vParamField = JSONUtils.parseObject(field, ValueParamField.class);
                vParamField.initialize();
                valueParamFields.add(vParamField);
            }
        }
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                if(fieldAction == FieldAction.REPLACE){
                    for (ValueParamField valueParamField : valueParamFields) {
                        item.put(valueParamField.getField().getCode(), valueParamField.replace(item));
                    }
                    nodeOutput.addResult(item);
                }else{
                    Map<String,Object> mv = new LinkedHashMap<>();
                    if(!outputRestricted){
                        mv.putAll(item);
                    }
                    for (ExtendField extendField : extendFields) {
                        mv.put(extendField.getCode(),extendField.compute(item));
                    }
                    nodeOutput.addResult(mv);
                }
            }//end executionInput
        }//end input
    }

    enum FieldAction {
        REPLACE,
        FUNCTION;
    }

}
