package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the standard deviation function is:
 * Standard deviation = sqrt(sum((x - mean)^2) / (n - 1))
 * Where x is a list of data points, mean is the mean of the data points, and n is the number of data points.
 * The standard deviation is a measure of how spread out the data is from the mean. If the data is tightly clustered around the mean, the standard deviation will be small. If the data is spread out over a large range, the standard deviation will be large.
 * @author Smars
 * @date 2023/8/29
 */
public class StdDeviationFunction extends AbstractAggregationFunction {


    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new StandardDeviation(),items);
    }
}
