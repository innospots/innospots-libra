package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * The percentile function returns the value at the specified percentile of a distribution.
 * For example, if you have a distribution of exam scores and you want to know what score corresponds to the 90th percentile,
 * you can use the percentile function to find it. The formula for the percentile function is:
 *
 * percentile(X, p) = X[(n * p) / 100]
 *
 * where:
 * - X is the vector of input values
 * - p is the percentile value (e.g., 90 for the 90th percentile)
 * - n is the number of data points in X
 * - [] is the flooring operator that rounds down to the nearest integer
 *
 * @author Smars
 * @date 2023/8/22
 */
public class PercentileFunction extends AbstractAggregationFunction {

    protected double quantile=50.0;

    public PercentileFunction(double quantile) {
        this.quantile = quantile;
    }

    public PercentileFunction() {
    }

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        Percentile percentile = new Percentile(quantile);
        return percentile.evaluate(toDoubleArray(items));
    }
}
