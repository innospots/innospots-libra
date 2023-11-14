package io.innospots.base.function.aggregation;

import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.IScriptExecutor;

/**
 * @author Smars
 * @date 2023/8/22
 */
public class AggregationFunctionBuilder {

    public static IAggregationFunction build(StatisticFunctionType functionType,
                                             ParamField summaryField,
                                             ParamField weightField,
                                             IScriptExecutor condition){
        IAggregationFunction function = null;
        try {
            function = functionType.aggFuncClass().newInstance();
            function.initialize(summaryField,weightField,condition);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return function;
    }
}
