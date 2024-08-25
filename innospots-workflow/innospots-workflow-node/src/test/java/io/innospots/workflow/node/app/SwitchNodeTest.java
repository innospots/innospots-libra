package io.innospots.workflow.node.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.map.MapUtil;
import com.alibaba.excel.util.BeanMapUtils;
import io.innospots.base.condition.*;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.utils.BeanUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.BeanMapping;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SwitchNodeTest {

    @Test
    void toBean(){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("name","int2");
        map.put("code","int2C");
        map.put("opt", Opt.GREATER.name());
        map.put("value",20);
        map.put("valueType", FieldValueType.INTEGER.name());

        Factor f = JSONUtils.mapToBean(map, Factor.class);

        System.out.println(f);
        List<Map<String, Object>> fms = List.of(map);
        Map<String,Object> cb = new LinkedHashMap<>();
        cb.put("factors",fms);
        cb.put("relation", Relation.AND.name());
        cb.put("mode", Mode.SCRIPT.name());
        BaseCondition condition = BeanUtils.toBean(cb, BaseCondition.class);
        condition.initialize();
        System.out.println(condition.getStatement());
        System.out.println(condition);
    }

    @Test
    void toBean2(){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("name","int2");
        map.put("code","int2C");
        map.put("opt", Opt.GREATER.name());
        map.put("value",20);
        map.put("valueType", FieldValueType.INTEGER.name());
        Factor factor = BeanUtil.toBean(map, Factor.class, CopyOptions.create().setAutoTransCamelCase(true).setConverter(new TypeConverter() {
            @Override
            public Object convert(Type type, Object o) {
                return o;
            }
        }));
        System.out.println(factor);
    }
}
