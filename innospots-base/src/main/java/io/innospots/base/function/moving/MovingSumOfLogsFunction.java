package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingSumOfLogsFunction extends AbstractMovingFunction{

    private SumOfLogs sumOfLogs = new SumOfLogs();

    public MovingSumOfLogsFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return computeStc(sumOfLogs);
    }
}
