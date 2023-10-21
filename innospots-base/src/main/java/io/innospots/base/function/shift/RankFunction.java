package io.innospots.base.function.shift;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/3
 */
public class RankFunction extends AbstractShiftFunction{


    public RankFunction(int window) {
        super(window);
    }

    @Override
    public Object[] compute(List<Map<String, Object>> items) {
        Object[] vs = new Object[items.size()];
        Object prev = null;
        int rank = 1;
        for (int i = 0; i < items.size(); i++) {
            Object crt = items.get(i).getOrDefault(fieldCode(),null);
            if(i==0){
                vs[i] = rank;
            }else if(crt!=null && crt.equals(prev)){
                vs[i] = rank;
            }else{
                rank++;
                vs[i] = rank;
            }
            prev = crt;
        }
        return vs;
    }
}
