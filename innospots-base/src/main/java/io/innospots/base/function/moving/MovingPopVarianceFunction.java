package io.innospots.base.function.moving;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingPopVarianceFunction extends AbstractMovingFunction{



    public MovingPopVarianceFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        double[] data = toArray();
        double variance = 0;
        double avg = Arrays.stream(data).average().orElse(0);
        for (int i = 0; i < data.length; i++) {
            variance = variance + (Math.pow((data[i] - avg), 2));
        }
        variance = variance / size();
        return variance;
    }
}
