package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.Collection;
import java.util.Map;

/**
 * The median function is often calculated using the following formula:
 * Sort the data points from smallest to largest.
 * If there is an even number of data points, the median is the average of the two middle data points.
 * If there is an odd number of data points, the median is the middle data point.
 * For example, if the data points are 1, 3, 5, 7, and 9, they are already in sorted order. The middle data point is 5, so the median is 5.
 * The median is a measure of the middle value in a distribution. It is useful for representing the "typical" value in a dataset that may have outliers.
 * @author Smars
 * @date 2023/8/30
 */
public class MedianFunction extends AbstractAggregationFunction {

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        Median median = new Median();
        return median.evaluate(toDoubleArray(items));
    }
}
