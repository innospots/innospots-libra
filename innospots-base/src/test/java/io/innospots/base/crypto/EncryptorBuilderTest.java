package io.innospots.base.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @date 2024/9/5
 */
class EncryptorBuilderTest {

    @Test
    void test1(){
        String key = "Q2K9aOslSxDv3wM5";
        IEncryptor encryptor = EncryptorBuilder.build(EncryptType.BLOWFISH,key);
        String enFromValue = "vHhmkDIwZ4jYPzQc9rrQaKuIDJ-JW1nFIHTjzu-9ieD9KhyV-jITtj4vkgk4sm473YRdZtLG4FeunPQ_MTzBTPotrBqtBJKhk6dwCJxufe1Xmn_ghvfFcyprr9GVqdylR_kMzsvPiLqEDFbUomUe-Q";
        String dval = encryptor.decode(enFromValue);
        System.out.println("解密字符串:");
        System.out.println(dval);
    }

}