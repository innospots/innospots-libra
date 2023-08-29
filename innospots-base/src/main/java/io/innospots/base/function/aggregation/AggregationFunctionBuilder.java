package io.innospots.base.function.aggregation;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.re.IExpression;

/**
 * @author Smars
 * @date 2023/8/22
 */
public class AggregationFunctionBuilder {

    public static IAggregationFunction build(AggregationFunctionType functionType, ParamField summaryField, IExpression<?> condition){
        IAggregationFunction function = null;
        try {
            function = functionType.functionClass().newInstance();
            function.initialize(summaryField,condition);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return function;
    }
}
