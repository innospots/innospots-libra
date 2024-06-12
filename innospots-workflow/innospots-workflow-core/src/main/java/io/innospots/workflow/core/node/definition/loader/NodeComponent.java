package io.innospots.workflow.core.node.definition.loader;

import io.innospots.base.utils.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/11
 */
@Getter
@Setter
public class NodeComponent {

    private String name;

    private String type;

    private String valueType;

    private boolean copyable;

    private boolean deletable;

    private Map<String,Object> settings;

    private Map<String,Object> layout;

    private List<CompCondition> conditions;

    public Map<String,Object> toMap(String code,int index){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("id",String.join("_", code,name.toLowerCase(), RandomStringUtils.randomAlphabetic(4), String.valueOf(index)));
        map.put("name",name);
        map.put("type",type);
        map.put("valueType",valueType);
        map.put("copyable",copyable);
        map.put("deletable",deletable);
        map.put("settings",settings);
        if(MapUtils.isNotEmpty(layout)){
            map.put("layout",layout);
        }
        return map;
    }

}
