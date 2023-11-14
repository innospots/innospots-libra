package io.innospots.base.utils;

import io.innospots.base.script.java.ParamName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/24
 */

public class Test_2 {
    private static Logger logger = LoggerFactory.getLogger("live.re.scripts.Test_1");

    public static Object $n892NodeKey(@ParamName("f1") String f1, @ParamName("f2") Integer f2) {
        try {
            return f1 + f2;
        } catch (Exception var3) {
            logger.error(var3.getMessage(), var3);
            return null;
        }
    }

    public static boolean $n892NodeKey_m_1(@ParamName("payload") Map payload) {
        try {
            byte f = 91;
            long f2 = 99L;
            long f3 = 5L;
            int f22 = f + 9;
        } catch (Exception var8) {
            logger.error(var8.getMessage(), var8);
        }

        return true;
    }

    public static Map $n892NodeKey_m_2(@ParamName("payload") Map ff) {
        try {
            double fs = 5.2D;
        } catch (Exception var4) {
            logger.error(var4.getMessage(), var4);
        }

        return ff;
    }

    public Test_2() {
    }
}