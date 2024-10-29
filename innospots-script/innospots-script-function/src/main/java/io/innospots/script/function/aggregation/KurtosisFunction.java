package io.innospots.script.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the kurtosis function is:
 *
 * kurtosis = (1/n) * sum((xi - mean)^4) / std^4 - 3
 *
 * where:
 * - kurtosis is the kurtosis of the distribution
 * - n is the number of data points
 * - xi is the ith data point
 * - mean is the mean of the data points
 * - std is the standard deviation of the data points
 *
 * @author Smars
 * @date 2023/8/30
 */
public class KurtosisFunction extends AbstractAggregationFunction {

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new Kurtosis(), items);
    }
}
