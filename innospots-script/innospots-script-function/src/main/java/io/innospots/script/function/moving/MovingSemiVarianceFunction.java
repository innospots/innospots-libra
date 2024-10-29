package io.innospots.script.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.SemiVariance;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingSemiVarianceFunction extends AbstractMovingFunction{

    SemiVariance semiVariance = new SemiVariance();

    public MovingSemiVarianceFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return semiVariance.evaluate(toArray());
    }
}
