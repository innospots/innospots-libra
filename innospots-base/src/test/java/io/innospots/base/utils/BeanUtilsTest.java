package io.innospots.base.utils;

import io.innospots.base.condition.Factor;
import io.innospots.base.condition.Opt;
import io.innospots.base.model.BaseModelInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/15
 */
public class BeanUtilsTest {

    @Test
    public void copyProperties() throws InvocationTargetException, IllegalAccessException {

        Map<String, Object> m = new HashMap<>();
//        m.put("name","dd");
//        m.put("code","cds");
//        m.put("value","09");
        m.put("opt", "EQUAL");
        Factor ff = new Factor();
        //BeanUtils.populate(ff,m);
        ff = BeanUtils.toBean(m, Factor.class);
        System.out.println(ff);

        m.put("createdTime", Timestamp.valueOf("2022-03-31 19:15:53"));
        BeanUtils.toBean(m, BaseModelInfo.class);
        Opt opt = Opt.IN;
        System.out.println(opt instanceof Enum);

    }
}