package io.innospots.workflow.node.app.compute;

import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.function.moving.IMovingFunction;
import io.innospots.base.function.moving.MovingFunctionBuilder;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.Pair;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.instance.model.NodeInstance;
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
public class FunctionField {

    protected ParamField field;

    protected ParamField weight;

    protected String fieldCode;

    protected StatisticFunctionType function;

    protected Integer winLength;


    public static List<FunctionField> buildFuncFields(NodeInstance ni, String fieldName) {
        List<Map<String, Object>> values = ni.valueList(fieldName);
        List<FunctionField> functionFields = new ArrayList<>();
        for (Map<String, Object> field : values) {
            functionFields.add(JSONUtils.parseObject(field, FunctionField.class));
        }
        return functionFields;
    }


    public static List<Pair<FunctionField, IMovingFunction>> buildMovingFunctions(
            NodeExecution nodeExecution,
            FuncType funcType,List<FunctionField> functionFields) {
        Integer total = null;
        if (funcType == FuncType.ACCUM) {
            total = nodeExecution.getInputs().stream().map(ExecutionInput::size).mapToInt(Integer::intValue).sum();
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

    public static void computeAccumAndRolling(NodeOutput nodeOutput, NodeExecution nodeExecution,
                                        FuncType funcType, List<FunctionField> functionFields,boolean outputRestricted, Logger logger) {
//        NodeOutput nodeOutput = buildOutput(nodeExecution);
        List<Map<String, Object>> outData = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        List<Pair<FunctionField, IMovingFunction>> movingFunctions = buildMovingFunctions(nodeExecution,funcType,functionFields);
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
                if (!outputRestricted) {
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

}
