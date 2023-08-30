package io.innospots.base.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Collection;
import java.util.Map;

/**
 * The formula for the standard error of the mean function is:
 * SE = σ / √n
 * where:
 * - SE is the standard error of the mean
 * - σ is the standard deviation
 * - n is the sample size
 * @author Smars
 * @date 2023/8/30
 */
public class StdErrMeanFunction extends AbstractAggregationFunction{

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        StandardDeviation stdDev = new StandardDeviation();
        for (Map<String, Object> item : items) {
            if(this.match(item)){
                stdDev.increment(value(item));
            }
        }
        return stdDev.getResult() / Math.sqrt(stdDev.getN());
    }
}
