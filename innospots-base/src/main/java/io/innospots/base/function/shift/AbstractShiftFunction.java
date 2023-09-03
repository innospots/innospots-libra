package io.innospots.base.function.shift;

import io.innospots.base.function.IStatisticFunction;
import io.innospots.base.model.field.ParamField;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/3
 */
public abstract class AbstractShiftFunction implements IShiftFunction {

    protected ParamField field;

    protected int window;

    public AbstractShiftFunction(int window) {
        this.window = window;
    }

    @Override
    public void initialize(ParamField field) {
        this.field = field;
    }

    @Override
    public int window() {
        return window;
    }

    @Override
    public String fieldCode() {
        return field.getCode();
    }

    protected Double value(Map<String,Object> item){
        return IStatisticFunction.v(item,fieldCode());
    }
}
