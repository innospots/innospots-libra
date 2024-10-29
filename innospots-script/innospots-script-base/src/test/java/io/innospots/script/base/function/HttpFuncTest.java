package io.innospots.script.base.function;


import org.junit.jupiter.api.Test;

import static io.innospots.script.base.function.HttpFunc.httpPost;

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