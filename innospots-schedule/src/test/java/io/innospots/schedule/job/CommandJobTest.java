package io.innospots.schedule.job;

import cn.hutool.core.util.RuntimeUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/2/26
 */
class CommandJobTest {

    @Test
    void test1(){
//        String[] cmds = new String[]{"echo", "$PWD"};
        String[] cmds = new String[]{"ls -l"};
        String mes = RuntimeUtil.execForStr(cmds);
        System.out.println(mes);
    }

}