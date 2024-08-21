package io.innospots.workflow.node.app.compute;

import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.function.moving.IMovingFunction;
import io.innospots.base.function.moving.MovingFunctionBuilder;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.Pair;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.exception.NodeFieldException;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import java.util.*;

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
            FuncType funcType, List<FunctionField> functionFields) {
        Integer total = nodeExecution.getInputs().stream().map(ExecutionInput::size).mapToInt(Integer::intValue).sum();
        return buildMovingFunctions(funcType, functionFields, total);
    }

    public static List<Pair<FunctionField, IMovingFunction>> buildMovingFunctions(
            FuncType funcType, List<FunctionField> functionFields, Integer total) {
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
                                              FuncType funcType, List<FunctionField> functionFields, boolean outputRestricted, Logger logger) {
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            try {
                List<Map<String, Object>> computedItems = computeAccumAndRolling(executionInput.getData(), funcType, functionFields, outputRestricted, logger);
                nodeOutput.addResult(computedItems);
            } catch (Exception e) {
                nodeExecution.setMessage(e.getMessage());
                nodeExecution.setStatus(ExecutionStatus.FAILED);
            }
        }//end for execution input
    }


    public static List<Map<String, Object>> computeAccumAndRolling(Collection<Map<String, Object>> items, FuncType funcType,
                                                                   List<FunctionField> functionFields, boolean outputRestricted, Logger logger) {
        List<Map<String, Object>> outData = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        List<Pair<FunctionField, IMovingFunction>> movingFunctions = buildMovingFunctions(funcType, functionFields, items.size());
        for (Map<String, Object> item : items) {
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
            }//end function pair
            if (!outputRestricted) {
                out.putAll(item);
            }
            outData.add(out);
        }//end for item
        if (!error.isEmpty()) {
            throw NodeFieldException.buildException(FunctionField.class, error.toString(), "");
        }
        return outData;
    }

}
