package io.innospots.script.function.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * The population variance function is usually calculated using the following formula:
 * Population variance = sum((x - mean(x))^2) / population size
 * where x is the set of data values, mean(x) is the mean of the data values, and population size is the number of data values in the set x.
 * This formula measures the spread of the data around the mean, and is a useful tool for statistical analysis.
 * @author Smars
 * @date 2023/8/29
 */
public class PopVarianceFunction extends AbstractAggregationFunction {


    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        double[] data = toDoubleArray(items);
        double variance = 0;
        double avg = Arrays.stream(data).average().orElse(0);
        for (int i = 0; i < data.length; i++) {
            variance = variance + (Math.pow((data[i] - avg), 2));
        }
        variance = variance / data.length;
        return variance;
    }
}
