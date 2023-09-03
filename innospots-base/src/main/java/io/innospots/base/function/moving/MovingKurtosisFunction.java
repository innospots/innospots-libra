package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingKurtosisFunction extends AbstractMovingFunction{

    private Kurtosis kurtosis = new Kurtosis();

    public MovingKurtosisFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return computeStc(kurtosis);
    }
}
