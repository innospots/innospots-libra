package io.innospots.script.function.moving;

import java.util.Map;

/**
 * The formula for a moving average function is:
 * <p>
 * moving average = (x1 + x2 + ... + xn) / n
 * <p>
 * Where:
 * <p>
 * - x1, x2, ..., xn are the data points to be averaged
 * - n is the number of data points in the moving average window.
 *
 * @author Smars
 * @date 2023/9/2
 */
public class MovingAvgFunction extends AbstractMovingFunction {

    private double sum = 0;

    public MovingAvgFunction(int size) {
        super(size);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        double value = value(input);
        if (values.size() == window && window > 0) {
            sum = sum - values.poll();
        }
        sum += value;
        values.offer(value);
        return sum / values.size();
    }
}
