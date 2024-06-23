package io.innospots.workflow.runtime.flow.node.data;

import org.junit.jupiter.api.Test;
import org.noear.snack.ONode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/10/28
 */
class ApiDataNodeTest {

    @Test
    void testJsonPath() {
        Map<String, Object> v = new LinkedHashMap<>();
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("agent", "abc");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "tim");
        body.put("age", "19");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("query", "12345");
        v.put("headers", headers);
        v.put("body", body);
        v.put("params", params);
        String extractJsonPath = "$.body";

        ONode jsonNode = ONode.load(v);
        Map value = jsonNode.select(extractJsonPath).toObject();
        System.out.println(value);
    }

}