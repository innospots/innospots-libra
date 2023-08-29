package io.innospots.base.function.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 *The population standard deviation function is usually calculated using the following formula:
 * Population standard deviation = sqrt(population variance)
 * where population variance is the value returned by the population variance function.
 * This formula measures the spread of the data around the mean, and is a useful tool for statistical analysis.
 * @author Smars
 * @date 2023/8/29
 */
public class PopStdDeviationFunction extends AbstractAggregationFunction {

    private PopVarianceFunction popVarianceFunction = new PopVarianceFunction();

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return Math.sqrt((Double) popVarianceFunction.compute(items));
    }
}
