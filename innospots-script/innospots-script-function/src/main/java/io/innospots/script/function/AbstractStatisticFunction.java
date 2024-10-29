package io.innospots.script.function;

import io.innospots.base.model.field.ParamField;
import io.innospots.script.base.IScriptExecutor;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/1
 */
public abstract class AbstractStatisticFunction<I> implements IStatisticFunction<I>{

    protected ParamField summaryField;

    protected ParamField weightField;

    protected IScriptExecutor condition;

    @Override
    public void initialize(ParamField summaryField, ParamField weightField, IScriptExecutor condition) {
        this.summaryField = summaryField;
        this.weightField = weightField;
        this.condition = condition;
    }

    @Override
    public boolean match(Map<String, Object> item) {
        if (condition != null) {
            return condition.executeBoolean(item);
        }
        return true;
    }

    @Override
    public String summaryFieldCode() {
        return summaryField.getCode();
    }

    @Override
    public String weightFieldCode() {
        if (weightField == null) {
            return null;
        }
        return weightField.getCode();
    }
}
