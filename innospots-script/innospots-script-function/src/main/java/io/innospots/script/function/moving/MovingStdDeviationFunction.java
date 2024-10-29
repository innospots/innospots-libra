package io.innospots.script.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Map;

/**
 *
 * @author Smars
 * @date 2023/9/2
 */
public class MovingStdDeviationFunction extends AbstractMovingFunction {

    private StandardDeviation standardDeviation = new StandardDeviation();

    public MovingStdDeviationFunction(int size) {
        super(size);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return computeStc(standardDeviation);
    }
}
