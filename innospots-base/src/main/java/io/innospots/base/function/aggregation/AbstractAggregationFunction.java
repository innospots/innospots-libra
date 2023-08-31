package io.innospots.base.function.aggregation;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.re.IExpression;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;

import java.util.Collection;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/22
 */
public abstract class AbstractAggregationFunction implements IAggregationFunction {

    protected ParamField summaryField;

    protected ParamField weightField;

    protected IExpression<?> condition;

    @Override
    public void initialize(ParamField summaryField, ParamField weightField, IExpression<?> condition) {
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

    protected double[] toDoubleArray(Collection<Map<String, Object>> items) {
        return items.stream()
                .filter(this::match)
                .mapToDouble(this::value).toArray();
    }

    protected double stcCompute(AbstractStorelessUnivariateStatistic statistic, Collection<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            if (this.match(item)) {
                statistic.increment(this.value(item));
            }
        }
        return statistic.getResult();
    }
}
