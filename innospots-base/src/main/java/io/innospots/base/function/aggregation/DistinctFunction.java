package io.innospots.base.function.aggregation;

import java.util.Collection;
import java.util.Map;

/**
 * The `Distinct Count` function returns the number of unique values in a list of values.
 * For example, if you have a list of names in column A, and you want to know how many unique names are there,
 * This formula will return the number of unique names in column A.
 * @author Smars
 * @date 2023/8/30
 */
public class DistinctFunction extends AbstractAggregationFunction{
    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return items.stream().filter(this::match)
                .map(item -> item.get(summaryField.getCode()))
                .distinct().count();
    }
}
