package io.innospots.script.function.aggregation;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The formula for the mean absolute deviation function is:
 * MAD = ∑ |x_i - mean(x)| / n
 *
 * where:
 * - MAD is the mean absolute deviation
 * - x_i is the ith data value
 * - mean(x) is the mean of the data set
 * - n is the number of data values in the set
 * @author Smars
 * @date 2023/8/30
 */
public class MeanAbsDeviationFunction extends AbstractAggregationFunction{

    @Override
    public Object compute(Collection<Map<String, Object>> items) {
        Mean mean = new Mean();
        List<Double> values = new ArrayList<Double>();
        for (Map<String, Object> item : items) {
            if(this.match(item)){
                Double dv = value(item);
                mean.increment(dv);
                values.add(dv);
            }
        }
        if(values.isEmpty()){
            return 0d;
        }
        Double meanVal = mean.getResult();
        Double absSum = 0d;
        for (Double value : values) {
            absSum += Math.abs(value - meanVal);
        }
        return absSum/values.size();
    }
}
