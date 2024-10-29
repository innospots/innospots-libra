package io.innospots.script.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.Skewness;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingSkewnessFunction extends AbstractMovingFunction{

    private Skewness skewness=new Skewness();

    public MovingSkewnessFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return computeStc(skewness);
    }
}
