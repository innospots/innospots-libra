package io.innospots.script.base.function;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import io.innospots.script.base.aviator.function.AviatorFunctionLoader;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/29
 */
class UdafCountTest {

    @BeforeAll
    static void setup() {
        AviatorFunctionLoader.load();
    }

    @Test
    void testCount() {
        AviatorFunction function = AviatorEvaluator.getFunction("af.count");
        System.out.println(function);
        Assertions.assertNotNull(function);
//        String exp = "af.count(items,groupField,conditions)";
        String exp = "af.count(items,condition,groupField)";
        Expression expression = AviatorEvaluator.compile(exp, true);
        Map<String, Object> env = new HashMap<>();
        List<Map<String, Object>> items = items();
        env.put("items", items);
        env.put("groupField", "stock_id");
        env.put("condition", "click_count<=3");
        for (Map<String, Object> item : items) {
            System.out.println(item);
        }
        Object v = expression.execute(env);
        System.out.println(v);
    }

    private List<Map<String, Object>> items() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("highest_price", RandomUtils.nextInt(30, 50));
            m.put("click_count", RandomUtils.nextInt(2, 9));
            m.put("stock_price", RandomUtils.nextDouble(5, 99));
            m.put("day", 3 + i);
            m.put("uid", "uuid_" + i);
            m.put("stock_id", "sid_" + i % 5);
            items.add(m);
        }
        return items;
    }
}