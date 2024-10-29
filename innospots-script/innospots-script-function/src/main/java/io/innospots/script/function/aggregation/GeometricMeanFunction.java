package io.innospots.script.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the geometric mean function is:
 *
 * geometric mean(x) = nth root(x_1 x_2 ... x_n)
 *
 * where:
 * - x is a vector of input values
 * - x_i is the ith input value
 * - n is the number of input values
 * - nth root is the nth root operator that compute the nth root of a value
 * @author Smars
 * @date 2023/8/30
 */
public class GeometricMeanFunction extends AbstractAggregationFunction{

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new GeometricMean(),items);
    }
}
