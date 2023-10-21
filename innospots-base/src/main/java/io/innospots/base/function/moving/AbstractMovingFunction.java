package io.innospots.base.function.moving;

import com.google.common.primitives.Doubles;
import io.innospots.base.function.AbstractStatisticFunction;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.DoubleFunction;

/**
 * @author Smars
 * @date 2023/9/1
 */
public abstract class AbstractMovingFunction extends AbstractStatisticFunction<Map<String, Object>> implements IMovingFunction {

    protected ArrayBlockingQueue<Double> values;

    protected int window;

    //The minimum number of observations that each window contains
    protected int periods = 1;

    //TODO period
    protected String periodUnit;

    public AbstractMovingFunction(int window) {
        this.window = window;
        values = new ArrayBlockingQueue<>(window);

    }

    protected double put(Map<String, Object> input){
        double value = value(input);
        if (values.size() == window && window > 0) {
            values.poll();
        }
        values.offer(value);
        return value;
    }

    protected double[] toArray(){
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    protected double computeStc(AbstractStorelessUnivariateStatistic statistic) {
        return statistic.evaluate(toArray());
    }

    @Override
    public int window() {
        return window;
    }

    public int size(){
        return values.size();
    }

    public enum PeriodUnit {
        DAY,
        WEEK,
        HOUR,
        SECOND,
        MINUTE,
        YEAR,
        STEP;
    }
}
