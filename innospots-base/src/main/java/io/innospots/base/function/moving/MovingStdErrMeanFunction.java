package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingStdErrMeanFunction extends AbstractMovingFunction{

    private StandardDeviation stdDev = new StandardDeviation();

    public MovingStdErrMeanFunction(int window) {
        super(window);
    }


    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        stdDev.setData(toArray());
        return stdDev.getResult() / Math.sqrt(stdDev.getN());
    }
}
