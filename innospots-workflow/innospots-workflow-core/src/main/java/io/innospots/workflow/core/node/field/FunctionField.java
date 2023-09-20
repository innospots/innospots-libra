package io.innospots.workflow.core.node.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.condition.Factor;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.exception.NodeFieldException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/20
 */
@Slf4j
public class FunctionField implements Initializer {


    private ValueParamField.ValueReplaceMethod method;

    private String code;

    /**
     * key:value,key:value
     */
    private String value;

    private Factor field;

    @JsonIgnore
    private Map<String,Object> dict;

    @Override
    public void initialize() {
        if(method == ValueParamField.ValueReplaceMethod.rp_dict){
            dict = new HashMap<>();
            if(StringUtils.isNotEmpty(value)){
                String[] vv = value.split(",");
                for (String v : vv) {
                    String[] dv = v.split(":");
                    if(dv.length>=2){
                        dict.put(dv[0],dv[1]);
                    }
                }
            }
        }
    }

    public Object compute(Map<String,Object> item){
        Object val = item.get(field.getCode());
        try {
            switch (method){
                case rp_rexp:
                    if(StringUtils.isNotEmpty(value) && val != null){
                        String[] vv = value.split(",");
                        val = String.valueOf(val).replaceAll(vv[0],vv[1]);
                    }
                    break;
                case rp_func:
                    log.warn("replace function field value:{}",this);
                    break;
                case rp_null:
                    if(val == null){
                        return value;
                    }
                    if(StringUtils.isEmpty(String.valueOf(val))){
                        return value;
                    }
                    break;
                case rp_dict:
                    val = dict.get(String.valueOf(val));
                default:
                    break;
            }
        }catch (Exception e){
            throw NodeFieldException.buildException(this.getClass(),e,"field replace fail,",item);
        }

        return val;
    }


    public enum FunctionName {
        crt_date,
        date_format,
        plus_year,
        plus_month,
        plus_date,
        plus_hour,
        regular_exp,
        dict_map;

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("method=").append(method);
        sb.append(", code='").append(code).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", field=").append(field);
        sb.append(", dict=").append(dict);
        sb.append('}');
        return sb.toString();
    }

}
