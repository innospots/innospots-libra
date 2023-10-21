package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the average function is:
 * Average = total / number of items
 * OR
 * Average = sum of (value * corresponding count) / total number of items
 * where total is the sum of all the values, number of items is the number of values, and Σ indicates the sum operator.
 * For example, if there are three values 2, 4, and 6, then their average is:
 * (2 + 4 + 6) / 3 = 4
 * @author Smars
 * @date 2023/8/29
 */
public class AverageFunction extends AbstractAggregationFunction{


    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new Mean(),items);
    }
}
