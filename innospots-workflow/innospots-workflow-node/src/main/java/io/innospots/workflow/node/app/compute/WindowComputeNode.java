package io.innospots.workflow.node.app.compute;

import io.innospots.base.function.moving.IMovingFunction;
import io.innospots.base.function.moving.MovingFunctionBuilder;
import io.innospots.base.model.Pair;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.innospots.workflow.node.app.compute.FunctionField.buildFuncFields;
import static io.innospots.workflow.node.app.compute.ShiftFunctionField.buildShiftFuncFields;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class WindowComputeNode extends BaseNodeExecutor {

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
    protected void initialize() {
        funcType = FuncType.valueOf(valueString(FUNC_TYPE));
        outputRestricted = valueBoolean(FIELD_OUTPUT_RESTRICTED);
        if (funcType == FuncType.ROLLING) {
            rollingFields = buildFuncFields(ni,ROLLING_FIELDS);
        } else if (funcType == FuncType.ACCUM) {
            accumFields = buildFuncFields(ni,ACCUM_FIELDS);
        } else if (funcType == FuncType.COLUMN) {
            shiftFields = buildShiftFuncFields(ni);
        }
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

        NodeOutput nodeOutput = this.buildOutput(nodeExecution);

        ShiftFunctionField.computeShift(nodeOutput,nodeExecution,this.shiftFields,outputRestricted,logger);
    }

    private void computeAccumAndRolling(NodeExecution nodeExecution) {

        NodeOutput nodeOutput = this.buildOutput(nodeExecution);

        List<FunctionField> functionFields = this.funcType == FuncType.ACCUM ? this.accumFields : this.rollingFields;

        FunctionField.computeAccumAndRolling(nodeOutput,nodeExecution,funcType,functionFields,this.outputRestricted,logger);
    }

}
