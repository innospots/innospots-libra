package io.innospots.connector.api.minder;

import cn.hutool.http.HttpStatus;
import io.innospots.base.data.enums.ApiMethod;
import io.innospots.base.data.http.HttpConnection;
import io.innospots.base.data.http.HttpData;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.json.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.noear.snack.ONode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Smars
 * @date 2023/7/20
 */
@Getter
@Setter
public class TokenHolder {

    public static final String TOKEN_ADDRESS = "access_token_url";
    public static final String ACCESS_TOKEN_URL_REQUEST_METHOD = "access_token_url_request_method";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TS = "token_ts";
    public static final String REQUEST_METHOD = "request_method";
    public static final String TOKEN_LOCATION = "token_location";
    public static final String TOKEN_PARAM = "token_param";
    public static final String EXPIRES_IN = "expires_in";
    public static final String TOKEN_PATH = "token_path";
    public static final String EXPIRES_PATH = "expires_path";
    public static final String REFRESH_TOKEN_PATH = "refresh_token_path";
    public static final String TOKEN_TYPE = "token_type";

    private ApiMethod apiMethod;

    private String tokenParam = "access_token";

    private int expiresIn;

    private String tokenPath = "$.access_token";

    private String expiresPath = "$.expires_in";

    private String refreshPath = "$.refresh_token";

    private long tokenTs;

    private TokenLocation tokenLoc = TokenLocation.PARAM;

    private String tokenAccessUrl;

    private String accessToken;

    private String refreshToken;

    private String queryParam;

    private String postBody;

    private HttpData response;

    private String tokenType;

    private HttpConnection httpConnection = new HttpConnection();

    public TokenHolder() {

    }

    public static TokenHolder build(String accessUrl, String queryParam, ApiMethod apiMethod) {
        TokenHolder th = new TokenHolder();
        th.setTokenAccessUrl(accessUrl);
        th.setQueryParam(queryParam);
        th.setApiMethod(apiMethod);
        return th;
    }

    public static TokenHolder build(ConnectionCredential connectionCredential) {
        TokenHolder holder = new TokenHolder();
        holder.setAccessToken(connectionCredential.v(ACCESS_TOKEN));
        holder.setRefreshToken(connectionCredential.v(REFRESH_TOKEN));
        Object ts = connectionCredential.value(TOKEN_TS);
        if (ts != null) {
            holder.setTokenTs((Long) ts);
        }
        holder.setExpiresIn(Integer.parseInt(connectionCredential.v(EXPIRES_IN, "600")));
        holder.setTokenAccessUrl(connectionCredential.v(TOKEN_ADDRESS));
        holder.setApiMethod(ApiMethod.valueOf(connectionCredential.v(REQUEST_METHOD, ApiMethod.POST.name())));
        holder.setTokenParam(connectionCredential.v(TOKEN_PARAM));
        holder.setTokenPath(connectionCredential.v(TOKEN_PATH, "$.access_token"));
        holder.setTokenLoc(TokenHolder.TokenLocation.valueOf(connectionCredential.v(TOKEN_LOCATION, TokenLocation.PARAM.name())));
        holder.setExpiresPath(connectionCredential.v(EXPIRES_PATH, "$.expires_in"));
        holder.setRefreshPath(connectionCredential.v(REFRESH_TOKEN_PATH, "$.refresh_token"));
        holder.setTokenType(connectionCredential.v(TOKEN_TYPE));

        return holder;
    }

    public Map<String, Object> buildAuthBody() {
        Map<String, Object> body = new HashMap<>();
        if (accessToken != null) {
            body.put(ACCESS_TOKEN, accessToken);
            body.put(EXPIRES_IN, this.expiresIn);
            body.put(TOKEN_TS, tokenTs);
        }
        if (refreshToken != null) {
            body.put(REFRESH_TOKEN, this.refreshToken);
        }
        if (response != null && response.getBody() instanceof Map) {
            body.putAll((Map<? extends String, ?>) response.getBody());
            if (this.tokenLoc == TokenLocation.PARAM) {
                body.remove(TOKEN_TYPE);
            }
        }
        return body;
    }

    /**
     * invoke remote access token address and return access token
     *
     * @param cache
     * @return
     */
    public String fetchToken(boolean cache) {
        long current = System.currentTimeMillis();
        long expireTs = tokenTs + expiresIn * 1000l;
        if (cache && accessToken != null && expireTs > current) {
            String token = "bearer".equalsIgnoreCase(tokenType) ? "Bearer " + accessToken : accessToken;
            return token;
        }
        Map<String, Object> params = new HashMap<>();
        if (this.queryParam != null) {
            String[] ps = this.queryParam.split("&");
            for (String p : ps) {
                String[] ss = p.split("=");
                params.put(ss[0], ss[1]);
            }
        }

        if (apiMethod == ApiMethod.GET) {
            response = httpConnection.get(tokenAccessUrl, params, null);
            extractToken(response);
        } else if (apiMethod == ApiMethod.POST) {
            response = httpConnection.post(tokenAccessUrl, params, this.postBody, null);
            extractToken(response);
        }

        String token = null;
        if (accessToken != null) {
            tokenTs = System.currentTimeMillis();
            token = "bearer".equalsIgnoreCase(tokenType) ? "Bearer " + accessToken : accessToken;
        }
        return token;
    }

    private void extractToken(HttpData httpData) {
        if (httpData.getStatus() != HttpStatus.HTTP_OK) {
            throw AuthenticationException.buildTokenInvalidException(
                    this.getClass(),
                    "access token is fail ",
                    httpData.getMessage(),
                    httpData.getBody(),
                    httpData.getStatus()
            );
        }
        ONode jsonNode = null;
        if (httpData.getBody() instanceof Map) {
            jsonNode = ONode.load(httpData.getBody());
        } else if (httpData.getBody() instanceof String) {
            String body = ((String) httpData.getBody());
            // url param format
            if (body.contains("&")) {
                Map<String, String> bodyMap = urlParamSplit(body);
                jsonNode = ONode.load(bodyMap);
            } else {
                // json str format
                jsonNode = ONode.load(JSONUtils.toMap(body));
            }
        } else {
            throw AuthenticationException.buildTokenInvalidException(this.getClass(), "access token is fail", httpData.getBody(), httpData.getMessage());
        }
        if (jsonNode != null) {
            ONode tokenONode = jsonNode.select(this.tokenPath);
            accessToken = tokenONode.isNull() ? null : tokenONode.toObject();
            fillExpires(jsonNode);
            if (refreshPath != null) {
                ONode refreshONode = jsonNode.select(this.refreshPath);
                refreshToken = refreshONode.isNull() ? null : refreshONode.toObject();
            }
        }
    }

    private Map<String, String> urlParamSplit(String param) {
        Map<String, String> mapRequest = new HashMap<>();
        String[] arrSplit = null;
        arrSplit = param.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            if (arrSplitEqual.length > 1) {
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (!Objects.equals(arrSplitEqual[0], "")) {
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    private void fillExpires(ONode jsonNode) {
        ONode expiresONode = jsonNode.select(this.expiresPath);
        Object ct = expiresONode.isNull() ? null : expiresONode;
        if (ct != null) {
            this.expiresIn = Integer.parseInt(ct.toString());
        } else {
            expiresONode = jsonNode.select("$.expires");
            ct = expiresONode.isNull() ? null : expiresONode;
            if (ct != null) {
                this.expiresIn = Integer.parseInt(ct.toString());
            }
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("accessToken='").append(accessToken).append('\'');
        sb.append(", expiresIn=").append(expiresIn);
        sb.append(", tokenTs=").append(tokenTs);
        sb.append(", apiMethod=").append(apiMethod);
        sb.append(", tokenParam='").append(tokenParam).append('\'');
        sb.append(", tokenPath='").append(tokenPath).append('\'');
        sb.append(", expiresPath='").append(expiresPath).append('\'');
        sb.append(", refreshPath='").append(refreshPath).append('\'');
        sb.append(", tokenLoc=").append(tokenLoc);
        sb.append(", tokenAccessUrl='").append(tokenAccessUrl).append('\'');
        sb.append(", queryParam='").append(queryParam).append('\'');
        sb.append(", postBody='").append(postBody).append('\'');
        sb.append(", response=").append(response);
        sb.append(", tokenType='").append(tokenType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public enum TokenLocation {
        HEADER,
        BODY,
        PARAM;
    }
}

