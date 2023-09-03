package io.innospots.workflow.node.app.compute;

import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.function.shift.IShiftFunction;
import io.innospots.base.function.shift.ShiftFunctionBuilder;
import io.innospots.base.function.shift.ShiftFunctionType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.Initializer;
import lombok.Getter;
import lombok.Setter;

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

    @Override
    public void initialize() {
        shiftFunction = ShiftFunctionBuilder.build(function,field,winLength);
    }
}
