package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.SemiVariance;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the semi variance function is:
 *
 * semi variance = E[(X - E[X])^2]
 *
 * where:
 * - semi variance is the semi variance of the random variable X
 * - E[X] is the expected value of X
 * - E[] is the expected value operator that returns the expected value of a random variable or expression
 *
 * @author Smars
 * @date 2023/8/30
 */
public class SemiVarianceFunction extends AbstractAggregationFunction{

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        SemiVariance semiVariance = new SemiVariance();
        return semiVariance.evaluate(toDoubleArray(items));
    }
}
