package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingSumOfSquareFunction extends AbstractMovingFunction{

    private SumOfSquares sumOfSquares = new SumOfSquares();

    public MovingSumOfSquareFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return computeStc(sumOfSquares);
    }
}
