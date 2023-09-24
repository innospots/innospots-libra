package io.innospots.workflow.core.node.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.condition.Factor;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.exception.NodeFieldException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/20
 */
@Getter
@Setter
@Slf4j
public class ValueParamField extends InputField implements Initializer {

    private ValueReplaceMethod method;

    @JsonIgnore
    private Map<String, Object> dict;

    @Override
    public void initialize() {
        if (method == ValueReplaceMethod.rp_dict) {
            dict = new HashMap<>();
            if (StringUtils.isNotEmpty(value)) {
                String[] vv = value.split(",");
                for (String v : vv) {
                    String[] dv = v.split(":");
                    if (dv.length >= 2) {
                        dict.put(dv[0], dv[1]);
                    }
                }
            }
        }
    }

    public Object replace(Map<String, Object> item) {
        Object val = item.get(field.getCode());
        try {
            switch (method) {
                case rp_rexp:
                    if (StringUtils.isNotEmpty(value) && val != null) {
                        String[] vv = value.split(",");
                        val = String.valueOf(val).replaceAll(vv[0], vv[1]);
                    }
                    break;
                case rp_null:
                    if (val == null) {
                        return value;
                    }
                    if (StringUtils.isEmpty(String.valueOf(val))) {
                        return value;
                    }
                    break;
                case rp_dict:
                    val = dict.get(String.valueOf(val));
                default:
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw NodeFieldException.buildException(this.getClass(), e, "field replace fail,", item);
        }

        return val;
    }


    public enum ValueReplaceMethod {
        //
        rp_null,
        rp_rexp,
        rp_dict;
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
