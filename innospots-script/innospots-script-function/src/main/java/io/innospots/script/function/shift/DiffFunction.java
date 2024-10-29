package io.innospots.script.function.shift;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/3
 */
public class DiffFunction extends AbstractShiftFunction{

    public DiffFunction(int window) {
        super(window);
    }

    @Override
    public Object[] compute(List<Map<String, Object>> items) {
        Object[] vs = new Object[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            if (window > 0 && i >= window) {
                Double crt = value(item);
                Double prev = value(items.get(i - window));
                vs[i] = crt - prev;
            } else if (window < 0 && (i - window) < items.size()) {
                Double crt = value(items.get(i - window));
                Double prev = value(item);
                vs[i] = crt - prev;
            }
        }
        return vs;
    }
}
