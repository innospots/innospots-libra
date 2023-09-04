package io.innospots.workflow.node.app.compute;

import io.innospots.base.function.moving.IMovingFunction;
import io.innospots.base.function.moving.MovingFunctionBuilder;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.Pair;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class WindowComputeNode extends BaseAppNode {

    private static final Logger logger = LoggerFactory.getLogger(WindowComputeNode.class);

    private static final String FUNC_TYPE = "func_type";

    private static final String ROLLING_FIELDS = "rolling_fields";

    private static final String ACCUM_FIELDS = "accum_fields";
    private static final String SHIFT_FIELDS = "shift_fields";

    /**
     * boolean value, only the variables in the variable list are output
     */
    public static final String FIELD_OUTPUT_RESTRICTED = "output_restricted";

    private FuncType funcType;

    private boolean outputRestricted;

    private List<FunctionField> rollingFields;

    private List<FunctionField> accumFields;

    private List<ShiftFunctionField> shiftFields;

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        funcType = FuncType.valueOf(valueString(FUNC_TYPE));
        outputRestricted = nodeInstance.valueBoolean(FIELD_OUTPUT_RESTRICTED);
        if (funcType == FuncType.ROLLING) {
            rollingFields = buildFuncFields(ROLLING_FIELDS);
        } else if (funcType == FuncType.ACCUM) {
            accumFields = buildFuncFields(ACCUM_FIELDS);
        } else if (funcType == FuncType.COLUMN) {
            shiftFields = buildShiftFuncFields();
        }
    }

    private List<ShiftFunctionField> buildShiftFuncFields() {
        List<Map<String, Object>> values = ni.valueList(SHIFT_FIELDS);
        List<ShiftFunctionField> functionFields = new ArrayList<>();
        for (Map<String, Object> field : values) {
            ShiftFunctionField s = JSONUtils.parseObject(field, ShiftFunctionField.class);
            s.initialize();
            functionFields.add(s);
        }
        return functionFields;
    }

    private List<FunctionField> buildFuncFields(String fieldName) {
        List<Map<String, Object>> values = ni.valueList(fieldName);
        List<FunctionField> functionFields = new ArrayList<>();
        for (Map<String, Object> field : values) {
            functionFields.add(JSONUtils.parseObject(field, FunctionField.class));
        }
        return functionFields;
    }

    private List<Pair<FunctionField, IMovingFunction>> buildMovingFunctions(NodeExecution nodeExecution) {
        Integer total = null;
        List<FunctionField> functionFields;
        if (funcType == FuncType.ACCUM) {
            functionFields = accumFields;
            total = nodeExecution.getInputs().stream().map(ExecutionInput::size).mapToInt(Integer::intValue).sum();
        } else {
            functionFields = rollingFields;
        }
        List<Pair<FunctionField, IMovingFunction>> movingFunctions = new ArrayList<>();
        for (FunctionField wField : functionFields) {
            Integer window = wField.getWinLength();
            if (funcType == FuncType.ACCUM && total != null) {
                window = total;
            }
            IMovingFunction moveFunction = MovingFunctionBuilder.
                    build(wField.getFunction(), window,
                            wField.getField(),
                            wField.getWeight());
            movingFunctions.add(Pair.of(wField, moveFunction));
        }
        return movingFunctions;
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        if (funcType == FuncType.COLUMN) {
            computeShift(nodeExecution);
        } else {
            computeAccumAndRolling(nodeExecution);
        }
    }

    private void computeShift(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = buildOutput(nodeExecution);
        List<Map<String, Object>> outData = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            outData.addAll(executionInput.getData());
        }//end for execution input
        List<Map<String, Object>> nFData = new ArrayList<>();
        for (ShiftFunctionField shiftField : shiftFields) {
            try {
                Object[] obj = shiftField.getShiftFunction().compute(outData);
                if (!this.outputRestricted) {
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

    private void computeAccumAndRolling(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = buildOutput(nodeExecution);
        List<Map<String, Object>> outData = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        List<Pair<FunctionField, IMovingFunction>> movingFunctions = buildMovingFunctions(nodeExecution);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                Map<String, Object> out = new LinkedHashMap<>();
                for (Pair<FunctionField, IMovingFunction> functionPair : movingFunctions) {
                    FunctionField functionField = functionPair.getLeft();
                    try {
                        Object result = functionPair.getRight().compute(item);
                        out.put(functionPair.getLeft().getFieldCode(), result);
                    } catch (Exception e) {
                        logger.error("compute field failed:{}, data:{}", functionField, item, e);
                        error.append(functionField.getFieldCode());
                        error.append(", ");
                        error.append(functionField.getFunction());
                        error.append(" ,error: ");
                        error.append(e.getMessage());
                    }

                }
                if (!this.outputRestricted) {
                    out.putAll(item);
                }
                outData.add(out);
            }//end for item
        }//end for execution input
        if (error.length() > 0) {
            nodeExecution.setMessage(error.toString());
            nodeExecution.setStatus(ExecutionStatus.FAILED);
        }
        nodeOutput.setResults(outData);
    }

    public enum FuncType {
        ROLLING,
        COLUMN,
        ACCUM;
    }
}
