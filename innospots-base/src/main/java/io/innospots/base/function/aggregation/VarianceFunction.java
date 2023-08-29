package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.Collection;
import java.util.Map;

/**
 * The variance function is often calculated using the following formula:
 * variance = 1/n * sum((x_i - mean)^2)
 * Where x_i are the data points, mean is the mean of the data points, and n is the number of data points.
 *
 * The variance is a measure of how spread out the data is from the mean. If the data is tightly clustered around the mean, the variance will be small. If the data is spread out over a large range, the variance will be large.
 * @author Smars
 * @date 2023/8/30
 */
public class VarianceFunction extends AbstractAggregationFunction{


    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new Variance(), items);
    }
}
