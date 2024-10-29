package io.innospots.script.function.aggregation;

import io.innospots.script.function.AbstractStatisticFunction;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;

import java.util.Collection;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/22
 */
public abstract class AbstractAggregationFunction extends AbstractStatisticFunction<Collection<Map<String,Object>>> implements IAggregationFunction {


    protected double stcCompute(AbstractStorelessUnivariateStatistic statistic, Collection<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            if (this.match(item)) {
                statistic.increment(this.value(item));
            }
        }
        return statistic.getResult();
    }
}
