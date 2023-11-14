package io.innospots.base.script.function;

import org.junit.Test;

/**
 * @author Smars
 * @date 2021/8/31
 */
public class RegularTest {

    @Test
    public void testExtract() {
        String input = Regular.extract("abcd09922ddd", "([\\d]+)");
        System.out.println(input);
    }

    @Test
    public void testReplace() {

        String input = Regular.replace("ddssssooww", "ssss", "98dfs");
        System.out.println(input);
    }

}