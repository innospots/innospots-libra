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

package io.innospots.libra.kernel.module.config.model;

import cn.hutool.extra.mail.MailAccount;
import io.innospots.base.json.annotation.MaskValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * email server config
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/3
 */
@Getter
@Setter
public class EmailServerInfo {

    @NotNull(message = "Email sender must not be null")
    @Size(max = 64, message = "Email sender length max 64")
    @Schema(title = "Email sender")
    private String sender;
    @NotNull(message = "Email account must not be null")
    @Size(max = 128, message = "Email account length max 128")
    @Schema(title = "Email account")
    private String emailAccount;
    @NotNull(message = "Email server must not be null")
    @Size(max = 128, message = "Email server length max 128")
    @Schema(title = "Email server")
    private String stmpServer;
    @NotNull(message = "Email server port must not be null")
    @Min(value = 1, message = "Email server port min 1")
    @Max(value = 9999, message = "Email server port max 9999")
    @Schema(title = "Email server")
    private Integer stmpPort;
    @NotNull(message = "Email user name must not be null")
    @Size(max = 64, message = "Email user name length max 64")
    @Schema(title = "Email server")
    private String userName;

    @Schema(title = "ssl enable")
    private boolean sslEnable=true;

    /**
     * pwd decrypt: RsaKeyManager.decrypt(pwd,privateKey)
     */
    @Schema(title = "Email server")
    @MaskValue
    private String password;


    public MailAccount toMailAccount(){
        MailAccount mailAccount = new MailAccount();
        mailAccount.setAuth(true);
        mailAccount.setFrom(sender);
        mailAccount.setHost(stmpServer);
        mailAccount.setPort(stmpPort);
        mailAccount.setUser(userName);
        mailAccount.setPass(password);
        mailAccount.setSslEnable(sslEnable);

        return mailAccount;
    }
}
