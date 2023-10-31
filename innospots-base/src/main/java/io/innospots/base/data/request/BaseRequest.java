package io.innospots.base.data.request;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.condition.Factor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Smars
 * @date 2023/10/28
 */
@Getter
@Setter
public class BaseRequest<Body> {

    protected String credentialKey;

    protected String operation;

    protected String targetName;

    protected String uri;

    protected Body body;

    @Schema(title = "primary fieldName")
    protected String keyColumn;

    @Schema(title = "header param")
    protected Map<String,String> headers;

    @Schema(title = "simple query param")
    protected Map<String,Object> query;

    @Schema(title = "query condition")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected List<Factor> conditions;

    protected String connectorName;

    @Schema(title = "the current number of pages")
    protected int page = 1;

    @Schema(title = "number of entries per page")
    protected int size = 20;

    @Schema(title = "sort field")
    protected String sort;

    @Schema(title = "collation (ASC for ascending, DESC for descending)")
    protected Boolean asc = true;


    public void addHeader(String key,Object value){
        if(headers==null){
            this.headers = new HashMap<>();
        }
        if(value == null){
            return;
        }
        String s = value.toString();
        if(StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(s)){
            this.headers.put(key, s);
        }
    }

    public void addQuery(String key, Object value){
        if(query==null){
            this.query = new HashMap<>();
        }
        if(value == null){
            return;
        }
        String s = value.toString();
        if(StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(s)){
            this.query.put(key, s);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRequest that = (ItemRequest) o;
        return Objects.equals(credentialKey, that.credentialKey) && Objects.equals(operation, that.operation) && Objects.equals(targetName, that.targetName) && Objects.equals(uri, that.uri) && Objects.equals(headers, that.headers) && Objects.equals(body, that.body) && Objects.equals(query, that.query) && Objects.equals(connectorName, that.connectorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentialKey, operation, targetName, uri, headers, body, query, connectorName);
    }

    public String key(){
        return DigestUtil.sha1Hex(toString());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("credentialKey=").append(credentialKey);
        sb.append(", operation='").append(operation).append('\'');
        sb.append(", targetName='").append(targetName).append('\'');
        sb.append(", uri='").append(uri).append('\'');
        sb.append(", headers=").append(headers);
        sb.append(", body=").append(body);
        sb.append(", query=").append(query);
        sb.append(", connectorName='").append(connectorName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
