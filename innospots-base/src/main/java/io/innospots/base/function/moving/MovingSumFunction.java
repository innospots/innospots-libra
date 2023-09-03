package io.innospots.base.function.moving;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingSumFunction extends AbstractMovingFunction{

    private double sum = 0;

    public MovingSumFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        double value = value(input);
        if (values.size() == window && window > 0) {
            sum = sum - values.poll();
        }
        sum += value;
        values.offer(value);
        return sum;
    }
}
