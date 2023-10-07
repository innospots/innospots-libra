package io.innospots.workflow.node.app.compute;

import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.function.shift.IShiftFunction;
import io.innospots.base.function.shift.ShiftFunctionBuilder;
import io.innospots.base.function.shift.ShiftFunctionType;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.instance.NodeInstance;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/3
 */
@Getter
@Setter
public class ShiftFunctionField implements Initializer {

    protected ParamField field;

    protected String fieldCode;

    protected ShiftFunctionType function;

    protected Integer winLength;

    private IShiftFunction shiftFunction;

    private static final String SHIFT_FIELDS = "shift_fields";

    @Override
    public void initialize() {
        shiftFunction = ShiftFunctionBuilder.build(function,field,winLength);
    }


    public static List<ShiftFunctionField> buildShiftFuncFields(NodeInstance ni) {
        List<Map<String, Object>> values = ni.valueList(SHIFT_FIELDS);
        List<ShiftFunctionField> functionFields = new ArrayList<>();
        for (Map<String, Object> field : values) {
            ShiftFunctionField s = JSONUtils.parseObject(field, ShiftFunctionField.class);
            s.initialize();
            functionFields.add(s);
        }
        return functionFields;
    }


    public static void computeShift(NodeOutput nodeOutput, NodeExecution nodeExecution,
                              List<ShiftFunctionField> shiftFields, boolean outputRestricted, Logger logger) {

        List<Map<String, Object>> outData = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            outData.addAll(executionInput.getData());
        }//end for execution input
        List<Map<String, Object>> nFData = new ArrayList<>();
        for (ShiftFunctionField shiftField : shiftFields) {
            try {
                Object[] obj = shiftField.getShiftFunction().compute(outData);
                if (!outputRestricted) {
                    for (int i = 0; i < obj.length; i++) {
                        outData.get(i).put(shiftField.getFieldCode(), obj[i]);
                    }
                    continue;
                }//end not outputRestricted
                if (nFData.isEmpty()) {
                    for (int i = 0; i < obj.length; i++) {
                        Map<String, Object> nItem = new LinkedHashMap<>();
                        nItem.put(shiftField.getFieldCode(), obj[i]);
                        nFData.add(nItem);
                    }
                } else {
                    for (int i = 0; i < obj.length; i++) {
                        nFData.get(i).put(shiftField.getFieldCode(), obj[i]);
                    }
                }//end not empty nFData
            } catch (Exception e) {
                logger.error("compute field failed:{}, data size:{}", shiftField, outData.size(), e);
                error.append(shiftField.getFieldCode());
                error.append(", ");
                error.append(shiftField.getFunction());
                error.append(" ,error: ");
                error.append(e.getMessage());
            }

        }//end for
        if (error.length() > 0) {
            nodeExecution.setMessage(error.toString());
            nodeExecution.setStatus(ExecutionStatus.FAILED);
        }
        if (nFData.isEmpty()) {
            nodeOutput.setResults(outData);
        } else {
            nodeOutput.setResults(nFData);
        }
    }
}
