package io.innospots.base.function.aggregation;

import java.util.Collection;
import java.util.Map;

/**
 * The mode function is often calculated using the following formula:
 * Count the number of times each data point occurs.
 * The mode is the data point that occurs most frequently.
 * For example, if the data points are 1, 2, 2, 3, 3, and 5, the count for each data point is 1, 2, 2, 1, 1, and 1. The data point that occurs most frequently is 2, so the mode is 2.
 * The mode is a measure of the most common value in a distribution. It is useful for representing the most common outcome in a dataset.
 * @author Smars
 * @date 2023/8/30
 */
public class ModeFunction extends AbstractAggregationFunction{
    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return null;
    }
}
