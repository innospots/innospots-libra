package io.innospots.workflow.node.app;

import org.junit.jupiter.api.Test;
import org.noear.snack.ONode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/6/18
 */
public class JsonPathTest {

    @Test
    public void test1(){
        Map<String,Object> b = new HashMap<>();
        Map<String,Object> f = new HashMap<>();
        f.put("uri","file://abc");
        f.put("name","file://abc");
        f.put("resourceId","abdds_key");
        b.put("files", f);
        ONode oNode = ONode.load(b);
        ONode on = oNode.select("$.files.uri");
        System.out.println(on.toString());
    }

}
