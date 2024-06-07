/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.base.crypto;

import io.innospots.base.exception.CryptoException;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/9/28
 */
public class EncryptorBuilder {

    public static IEncryptor encryptor;

    public static void initialize(String secretKey){
        if(encryptor==null){
            encryptor = build(EncryptType.BLOWFISH, secretKey);
        }
    }


    public static IEncryptor build(EncryptType encryptType, String secretKey) {
        switch (encryptType) {
            case BLOWFISH:
                return new BlowFishEncryptor(secretKey);
            case AES:
                return new AesEncryptor(secretKey);
            case BASE64:
                return new Base64Encryptor();
            case LITERAL:
                return new LiteralEncryptor();
            default:
                throw CryptoException.buildException(EncryptorBuilder.class, null, "not support crypto", encryptType);
        }
    }
}
