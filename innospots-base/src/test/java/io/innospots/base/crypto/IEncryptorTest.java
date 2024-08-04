package io.innospots.base.crypto;

import cn.hutool.core.codec.Base64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @date 2023/6/16
 */
class IEncryptorTest {

    @Test
    void encode() {
        String path = "/Files/temp/User.png";
        String ss = Base64.encodeUrlSafe(path.getBytes());
        System.out.println(ss);
        System.out.println(new String(Base64.decode(ss.getBytes())));
    }
}