package io.innospots.base.function.moving;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingCountFunction extends AbstractMovingFunction{

    public MovingCountFunction(int window) {
        super(window);
    }

    @Override
    public Object compute(Map<String, Object> input) {
        put(input);
        return values.stream().filter(v->v!=null && !v.equals(0d)).count();
    }
}
