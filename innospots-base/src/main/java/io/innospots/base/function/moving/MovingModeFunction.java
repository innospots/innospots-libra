package io.innospots.base.function.moving;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Smars
 * @date 2023/9/2
 */
public class MovingModeFunction extends AbstractMovingFunction {

    Map<Object, Long> counter = new HashMap<>();

    public MovingModeFunction(int size) {
        super(size);
    }

    @Override
    public Object compute(Map<String, Object> input) {
         Double val = put(input);
         Long adder = counter.get(val);
         if(adder!=null){
             adder = adder + 1;
         }else{
             adder = 1l;
         }
         counter.put(val,adder);
        return Collections.max(counter.entrySet(),Map.Entry.comparingByValue()).getKey();
    }
}
