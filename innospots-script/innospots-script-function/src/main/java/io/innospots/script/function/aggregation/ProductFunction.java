package io.innospots.script.function.aggregation;

import org.apache.commons.math3.stat.descriptive.summary.Product;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the product function is:
 * product(x_1, x_2, ..., x_n) = x_1 x_2 ... x_n
 * where x_1, x_2, ..., x_n are input values.
 * The product function returns the product of all the input values.
 * @author Smars
 * @date 2023/8/30
 */
public class ProductFunction extends AbstractAggregationFunction{
    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new Product(),items);
    }
}
