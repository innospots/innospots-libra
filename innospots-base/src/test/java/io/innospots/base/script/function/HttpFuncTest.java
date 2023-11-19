package io.innospots.base.script.function;


import org.junit.jupiter.api.Test;

import static io.innospots.base.script.function.HttpFunc.httpPost;

/**
 * @author Smars
 * @date 2021/7/3
 */
public class HttpFuncTest {

    @Test
    void testPost() {
        httpPost("", null, "");
    }
}