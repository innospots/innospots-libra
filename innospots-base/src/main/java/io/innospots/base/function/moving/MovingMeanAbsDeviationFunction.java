package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingMeanAbsDeviationFunction extends AbstractMovingFunction {

    private Mean mean = new Mean();

    public MovingMeanAbsDeviationFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);

        Double meanVal = mean.evaluate(toArray());
        Double absSum = 0d;
        for (Double value : values) {
            absSum += Math.abs(value - meanVal);
        }
        return absSum / size();
    }
}
