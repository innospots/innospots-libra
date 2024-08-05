package io.innospots.libra.base.configuration;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @date 2024/8/5
 */
class AuthPropertiesTest {

    @Test
    void test(){
        String r = "innospots-2024-9";
//        String r = "innospots.com-2024-copyright-927";
        System.out.println(r.length());
        String s = HexUtil.encodeHexStr(r);
        System.out.println(s.length());
        System.out.println(s);
        System.out.println(s.getBytes().length);
    }

}