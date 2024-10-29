package io.innospots.script.function.moving;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
