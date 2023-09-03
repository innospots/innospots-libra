package io.innospots.base.function.shift;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/3
 */
public class ShiftFunction extends AbstractShiftFunction {


    public ShiftFunction(int window) {
        super(window);
    }

    @Override
    public Object[] compute(List<Map<String, Object>> items) {
        Object[] vs = new Object[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Map<String,Object> item = items.get(i);
            if(window<0 &&(i-window)<items.size()){
                vs[i] = items.get(i-window);
            }else{
                vs[i+window] = item;
            }
        }
        return vs;
    }
}
