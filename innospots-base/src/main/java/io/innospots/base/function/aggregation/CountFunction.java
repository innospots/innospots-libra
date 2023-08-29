package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the COUNT function is:
 * COUNT(x) = Number of elements
 * where x is a set or an array, and COUNT(x) returns the number of elements in x.
 * For example, if x = {1, 2, 3}, then COUNT(x) = 3.
 * @author Smars
 * @date 2023/8/29
 */
public class CountFunction extends AbstractAggregationFunction{


    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return items.stream().filter(this::match).count();
    }
}
