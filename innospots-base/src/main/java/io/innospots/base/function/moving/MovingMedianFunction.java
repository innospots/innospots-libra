package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.Map;

/**
 *
 * @author Smars
 * @date 2023/9/2
 */
public class MovingMedianFunction extends AbstractMovingFunction {

    private Median median = new Median();

    public MovingMedianFunction(int size) {
        super(size);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);

        return median.evaluate(toArray());
    }
}
