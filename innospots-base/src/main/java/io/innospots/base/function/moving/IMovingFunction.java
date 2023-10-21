package io.innospots.base.function.moving;

import io.innospots.base.function.IStatisticFunction;

import java.util.Map;

/**
 * Rolling is a function that calculates a certain value continuously in a data set.
 * @author Smars
 * @date 2023/9/1
 */
public interface IMovingFunction extends IStatisticFunction<Map<String,Object>> {

    /**
     * rolling window size
     * @return
     */
    int window();


}
