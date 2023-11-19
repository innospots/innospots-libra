package io.innospots.libra.security.rsa;

import io.innospots.base.crypto.RsaKeyManager;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @author Smars
 * @date 2021/9/14
 */
public class RsaKeyManagerTest {

    @Test
    public void encrypt() {
        Pair<String, String> keyPair = RsaKeyManager.generateKey();
        System.out.println(keyPair);
        System.out.println(keyPair.getLeft());
        String pwd = "UPwd!@#12345";
        String enCode = RsaKeyManager.encrypt(pwd, keyPair.getLeft());
        System.out.println(enCode);
        String deCode = RsaKeyManager.decrypt(enCode, keyPair.getRight());
        System.out.println(deCode);
        Assertions.assertEquals(pwd, deCode);
    }

    @Test
    public void test2() {
        String pk = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEApSuqtkSG4SdEEqvvodlZMxgxX6TW8V0BU0KLd+URRTOnr4UrjVLvCstJ5EBrX1VkjDHGgQ2fkBq9RHwPoUxoEv/OgHkRecNEjTvSJTKW7FXA4CjlaCZhRm7J+qxUTIOX4Pa/qhXvDKkvjAw09JNQC9aYLDBReMMClms+/5KuN/B1Gcxv4WbDFztswmzX/wuInM5VmSFo8ziNCmOG4ki07y02WzlVapHjKbJ5OaFfKfA7A5hd1gfTJlPIChgLxU0d28PHD43i+kdixwr19Gw5zycPND/ODMJ/6ptZv1kg21A/Tn+fHBYgJHjXCHVRbiLtg1UklvOPsfkmqvvUhOooL8Mniwg0i0ZtmkJ8ZjbdUT67cVp68Z+LjGZc5A3y38tRYhtvGEOOXK7m3Q6UUG6RO7SS41mF6LO9u8NKJyRv9+al45yi7l4Jaoh56smaunNwzOtvPceQxgUkaJ8PzfrHdJOVwQ7k8ZxNV27utHfhOtWQeI0ftGReepqzPAPIrhWQucnavGkBDRmEgg3EHIe03q39l8p2PVh4TaXb2mcksLb1nPiq37MZzBQOSOsVQcz8IJmGwsPDmyq9KeVABBZA6hj8sY3iBIFm3BTNoWdW62xqQ07wxE8qcdKnzstTfoPlkZ4edkdg9SDUZgBEvpteLPvpX8fp5e37MICffLMWclsCAwEAAQ==";
        String pr = "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";
        String pwd = "{\"u\":\"admin\",\"p\":\"123456\",\"s\":\"\",\"c\":\"7487B665B34CA352DF6E5577A03FF815\"}";
        String encode = RsaKeyManager.encrypt(pwd, pk);
        System.out.println(encode);
        String decode = RsaKeyManager.decrypt(encode, pr);
        System.out.println(decode);

        String eStr = "VWdGkcTo1/W4zogdw75s9Rz8dGFjIm1qbrSQ8s6dXPcgLFpKn/jpbR87vmqnXMoxYqOthshttw6yVNh5xV3i+X6aE8dowD718zc6qXkzL+DPSpa7LRufNR/fCauSAl2lB+Xli/UY98clmw4ZpOOIcDRNHkOf2lWCJalM4IkU0+VVnQj0iOUGU6RD5qv/ewWOVF8qFVF+t5EUHfBBJAJ8fONYulueCCmZReAULWLImA9ST6Ve3AcUSwGD4SQ8TzLIoUwMNi7t1X5dVz51ksis2Tb8JoE4bmdH6qgExqTb2/W89Rpd4G3R0Df0CdF/Sf5l1kk5v2sVCkGf/OOaUotuspHXNYqYpAGh164bZo1FIAVCwPB6XI2r6FC3h/R/m2vBIisuH9elUs/UC8hh0ngszpziq0Iu7xPm57U3VV4xpuqFJex23HTE4YLO5nFondlp1A58EscEcB5to50Jh4zhkUaRwXicmVAxnMOGbJsz0xSjCEm30F0L48q1fGRuIB9kt2MbSOHsushgpVNUy36TrExBnLxsX3RdKs7ajbEvOD+B4vCAsfrX52Z97hya+7C7JIsQts4YIsJCteJ/QJ9ctDXHvoU4z7xxEv5vwWdOLEgkNTvqXvfzsZlcCmPrm3uL2YiYXTozqGqESx/sgYpPuJMNOIIzoak0xl5mntNaNsY=";
        String deStr = RsaKeyManager.decrypt(eStr, pr);
        System.out.println(deStr);
    }
}