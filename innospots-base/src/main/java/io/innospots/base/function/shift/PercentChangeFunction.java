package io.innospots.base.function.shift;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/3
 */
public class PercentChangeFunction extends AbstractShiftFunction {


    public PercentChangeFunction(int window) {
        super(window);
    }

    @Override
    public Object[] compute(List<Map<String, Object>> items) {
        Object[] pct = new Object[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            if (window > 0 && i >= window) {
                Double crt = value(item);
                Double prev = value(items.get(i - window));
                pct[i] = crt / prev - 1;
            } else if (window < 0 && (i - window) < items.size()) {
                Double crt = value(items.get(i - window));
                Double prev = value(item);
                pct[i] = crt / prev - 1;
            }
        }
        return pct;
    }
}
