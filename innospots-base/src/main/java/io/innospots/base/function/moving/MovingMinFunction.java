package io.innospots.base.function.moving;

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
public class MovingMinFunction extends AbstractMovingFunction {

    private Double min = null;

    public MovingMinFunction(int size) {
        super(size);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        double value = put(input);
        if(min == null){
            min = value;
        }else{
            min = Double.min(min, value);
        }
        return min;
    }
}
