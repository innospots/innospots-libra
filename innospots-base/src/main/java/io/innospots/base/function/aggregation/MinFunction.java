package io.innospots.base.function.aggregation;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the MAX function is:
 * MAX(x) = the largest value in the set x
 *
 * where x is a set of values. MAX returns the largest value in the set x.
 *
 * For example, if x = {2, 4, 6, 8}, then MAX(x) = 8.
 * @author Smars
 * @date 2023/8/29
 */
public class MinFunction extends AbstractAggregationFunction {

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return items.stream()
                .filter(this::match)
                .map(item -> item.get(summaryField.getCode()))
                .min((o1, o2) -> {
                    if (o1 instanceof Comparable && o2 instanceof Comparable) {
                        return ((Comparable) o1).compareTo(o2);
                    }
                    return 0;
                }).orElse(null);
    }
}
