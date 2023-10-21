package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.summary.Product;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the skewness function is:
 *
 * skewness = ((1/n) * sum((xi - mean)^3) / std^3
 *
 * where:
 * - skewness is the skewness of the distribution
 * - n is the number of data points
 * - xi is the ith data point
 * - mean is the mean of the data points
 * - std is the standard deviation of the data points
 *
 * @author Smars
 * @date 2023/8/30
 */
public class SkewnessFunction extends AbstractAggregationFunction {

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new Skewness(), items);
    }
}
