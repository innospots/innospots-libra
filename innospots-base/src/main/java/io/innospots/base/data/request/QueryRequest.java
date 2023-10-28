package io.innospots.base.data.request;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/10/28
 */
public class QueryRequest extends BaseRequest<Map<String, Object>> {

    public void add(String key, Object value) {
        if(this.body == null){
            this.body = new HashMap<>();
        }
        this.body.put(key, value);
    }

    public void add(Map<String, ? extends Object> item) {
        if(this.body == null){
            this.body = new HashMap<>();
        }
        this.body.putAll(item);
    }
}
