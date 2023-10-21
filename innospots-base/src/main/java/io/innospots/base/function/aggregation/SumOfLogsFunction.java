package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the sum of logs function is:
 *
 * sum of logs(x) = sum(log(xi))
 *
 * where:
 * - x is a vector of input values
 * - xi is the ith input value
 * - log is the natural logarithm function that returns the logarithm of a value
 * - sum is the mathematical sum operator that adds up the terms in the expression
 *
 * @author Smars
 * @date 2023/8/30
 */
public class SumOfLogsFunction extends AbstractAggregationFunction{

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new SumOfLogs(),items);
    }
}
