package io.innospots.base.utils.http;

import io.innospots.base.json.JSONUtils;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * @author Smars
 * @date 2024/9/4
 */
public class HttpDataResponseHandler implements HttpClientResponseHandler<HttpData> {

    @Override
    public HttpData handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
        HttpEntity entity = response.getEntity();
        HttpData httpData = new HttpData();
        int statusCode = response.getCode();
        httpData.setStatus(statusCode);
        httpData.setMessage(response.getReasonPhrase());
        for (Header header : response.getHeaders()) {
            httpData.addHeader(header.getName(), header.getValue());
        }
        if (response.getCode() >= 300) {
            EntityUtils.consume(entity);
        } else {
            return entity == null ? null : this.handleEntity(httpData,entity);
        }
        return null;
    }


    private HttpData handleEntity(HttpData httpData,HttpEntity entity) throws IOException, ParseException {
        String result = EntityUtils.toString(entity);
        if (result.startsWith("[")) {
            httpData.setBody(JSONUtils.toList(result, LinkedHashMap.class));
        } else if (result.startsWith("{")) {
            httpData.setBody(JSONUtils.toMap(result));
        } else {
            httpData.setBody(result);
        }
        return httpData;
    }
}
