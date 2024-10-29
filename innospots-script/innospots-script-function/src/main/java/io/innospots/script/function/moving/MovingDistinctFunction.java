package io.innospots.script.function.moving;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingDistinctFunction extends AbstractMovingFunction{

    private Set<Object> dataSet = new HashSet<>();

    public MovingDistinctFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        Object val = put(input);
        dataSet.add(val);
        return dataSet.size();
    }
}
