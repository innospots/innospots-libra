package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.Map;

/**
 * The formula for a moving min function is:
 * <p>
 * moving min = Min(x1 + x2 + ... + xn)
 * <p>
 * Where:
 * <p>
 * - x1, x2, ..., xn are the data points to select min value
 * - n is the number of data points in the moving window.
 *
 * @author Smars
 * @date 2023/9/2
 */
public class MovingVarianceFunction extends AbstractMovingFunction {

    private Variance variance = new Variance();

    public MovingVarianceFunction(int size) {
        super(size);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return computeStc(variance);
    }
}
