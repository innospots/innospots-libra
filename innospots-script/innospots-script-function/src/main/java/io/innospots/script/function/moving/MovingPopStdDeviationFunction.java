package io.innospots.script.function.moving;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingPopStdDeviationFunction extends AbstractMovingFunction{

    private MovingPopVarianceFunction movingPopVarianceFunction;

    public MovingPopStdDeviationFunction(int window) {
        super(window);
        movingPopVarianceFunction = new MovingPopVarianceFunction(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        Double x = (Double) movingPopVarianceFunction.compute(input);
        return Math.sqrt(x);
    }
}
