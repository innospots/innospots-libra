package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the sum of squares function is:
 *
 * sum of squares(x) = sum(xi^2)
 *
 * where:
 * - x is a vector of input values
 * - xi is the ith input value
 * - sum is the mathematical sum operator that adds up the terms in the expression
 *
 * @author Smars
 * @date 2023/8/30
 */
public class SumOfSquareFunction extends AbstractAggregationFunction{

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        return stcCompute(new SumOfSquares(),items);
    }
}
