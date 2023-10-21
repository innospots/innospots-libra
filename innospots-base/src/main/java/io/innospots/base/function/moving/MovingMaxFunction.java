package io.innospots.base.function.moving;

import java.util.Map;

/**
 * The formula for a moving max function is:
 * <p>
 * moving max = Max(x1 + x2 + ... + xn)
 * <p>
 * Where:
 * <p>
 * - x1, x2, ..., xn are the data points to select max value
 * - n is the number of data points in the moving window.
 *
 * @author Smars
 * @date 2023/9/2
 */
public class MovingMaxFunction extends AbstractMovingFunction {

    private double max = 0;

    public MovingMaxFunction(int size) {
        super(size);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        double value = put(input);
        max = Double.max(max,value);
        return max;
    }
}
