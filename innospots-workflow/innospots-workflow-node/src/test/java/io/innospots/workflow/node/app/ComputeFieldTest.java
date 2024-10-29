package io.innospots.workflow.node.app;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.compute.ComputeItem;
import io.innospots.base.model.field.compute.ItemElement;
import io.innospots.workflow.node.app.compute.ComputeField;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yxy
 * @version 3.2.0
 * @date 2021/8/24
 */
public class ComputeFieldTest {

    @Test
    public void toExpScript() {

        ComputeField cf = cf();
        System.out.println(cf.toExpScript("aviator"));

        List<ComputeField> lcf = new ArrayList<>();
        lcf.add(cf);
        System.out.println(JSONUtils.toJsonString(lcf));
    }

    private ComputeField cf() {
        List<ComputeItem> cl = new ArrayList<>();
        ComputeItem c = ComputeItem.build(ItemElement.input)
                .value("10", "10").id(RandomStringUtils.randomAlphabetic(6));
        cl.add(c);
        c = ComputeItem.build(ItemElement.operator).value("+", "+").id(RandomStringUtils.randomAlphabetic(6));
        cl.add(c);
        c = ComputeItem.build(ItemElement.input).value("20", "20").id(RandomStringUtils.randomAlphabetic(6));
        cl.add(c);
        c = ComputeItem.build(ItemElement.operator).value("/", "/").id(RandomStringUtils.randomAlphabetic(6));
        cl.add(c);
        c = ComputeItem.build(ItemElement.function).value("string.indexOf(s1,s2)", "string.indexOf(${s1},${s2})").id(RandomStringUtils.randomAlphabetic(6));
        cl.add(c);
        ComputeItem cc = ComputeItem.build(ItemElement.field).value("user_number", "user_number").id(RandomStringUtils.randomAlphabetic(6));
        c.addItem(cc);
        cc = ComputeItem.build(ItemElement.input).value("'2'", "'2'").id(RandomStringUtils.randomAlphabetic(6));
        c.addItem(cc);

        c = ComputeItem.build(ItemElement.operator).value("+", "+").id(RandomStringUtils.randomAlphabetic(6));
        cl.add(c);

        c = ComputeItem.build(ItemElement.function).value("cmp(x, y)", "cmp(${x},${y})").id(RandomStringUtils.randomAlphabetic(6));
        cl.add(c);
        cc = ComputeItem.build(ItemElement.function).value("rand(n)", "rand(${n})").id(RandomStringUtils.randomAlphabetic(6));
        cc.addItem(ComputeItem.build(ItemElement.field).value("pos", "pos")).id(RandomStringUtils.randomAlphabetic(6));
        c.addItem(cc);
        cc = ComputeItem.build(ItemElement.function).value("long(v)", "long(${n})").id(RandomStringUtils.randomAlphabetic(6));
        cc.addItem(ComputeItem.build(ItemElement.input).value("91", "91")).id(RandomStringUtils.randomAlphabetic(6));
        c.addItem(cc);
        ComputeField cf = new ComputeField();
        cf.setCode("cfCode");
        cf.setFieldId(221);
        cf.setName("字段名称");
        cf.setValueType(FieldValueType.INTEGER);
        cf.setComputeItems(cl);
        return cf;
    }


    @Test
    public void testScript() {
        String s = cf().toExpScript("aviator");
        System.out.println(s);
        Expression exp = AviatorEvaluator.compile(s);
        Map<String, Object> env = new HashMap<>();
        env.put("user_number", "112201");
        env.put("pos", 200);
        Object v = exp.execute(env);

        System.out.println(v);
    }
}