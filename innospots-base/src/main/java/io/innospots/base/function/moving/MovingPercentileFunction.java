package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingPercentileFunction extends AbstractMovingFunction{

    protected double quantile=50.0;

    private Percentile percentile;

    public MovingPercentileFunction(int window, double quantile) {
        super(window);
        this.quantile = quantile;
        percentile= new Percentile(quantile);
    }

    public MovingPercentileFunction(int window) {
        this(window,50.0d);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return percentile.evaluate(toArray());
    }
}
