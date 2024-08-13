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

package io.innospots.libra.kernel.module.extension.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.BaseEntity;
import io.innospots.libra.base.extension.ExtensionSource;
import io.innospots.libra.base.extension.ExtensionStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.DigestUtils;

import jakarta.persistence.*;
import java.time.LocalDate;

import static io.innospots.libra.kernel.module.extension.entity.ExtDefinitionEntity.TABLE_NAME;

/**
 * The definition information of the extension in the current service includes the core extension of the system (extSource is core) and the extension downloaded from the public extension market (ext source is market)
 *
 * @author Smars
 * @date 2021/12/1
 */
@Getter
@Setter
@Entity
@Table(name = TABLE_NAME)
@TableName(TABLE_NAME)
public class ExtDefinitionEntity extends BaseEntity {

    public static final String TABLE_NAME = "ext_definition";

    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer extDefinitionId;

    @Column(length = 64)
    private String extKey;

    @Column(length = 128)
    private String extIcon;

    @Column(length = 128)
    private String extName;

    @Column(length = 16)
    private String extVersion;

    @Column
    private LocalDate publishTime;

    @Column(length = 32)
    private String vendor;

    @Column(length = 16)
    private String kernelVersion;

    @Column(length = 256)
    private String description;

    @Column(length = 16)
    private String lastAppVersion;

    @Column(length = 256)
    private String lastDescription;

    @Column
    private LocalDate lastPublishTime;

    @Column(length = 32)
    private String author;

    /**
     * LOADED   AVAILABLE   EXPIRED
     */
    @Column(length = 16)
//    @Enumerated(EnumType.STRING)
    private String extensionStatus;
//    private ExtensionStatus extensionStatus;

    /**
     * md5 signature from toString method
     */
    @Column(length = 64)
    private String signature;

    @Column(length = 16)
//    @Enumerated(EnumType.STRING)
    private String extensionSource;
//    private ExtensionSource extensionSource;

    /**
     * files of publish zip package
     */
    @Column(length = 512)
    private String zipFileNames;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("extKey='").append(extKey).append('\'');
        sb.append(", extName='").append(extName).append('\'');
        sb.append(", extVersion='").append(extVersion).append('\'');
        sb.append(", publishTime=").append(publishTime);
        sb.append(", vendor='").append(vendor).append('\'');
        sb.append(", kernelVersion='").append(kernelVersion).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String generateSignature() {
        if (signature == null) {
            String key = extKey;
            key += extName;
            key += extVersion;
            key += vendor;
            key += kernelVersion;
            key += author;
            setSignature(DigestUtils.md5DigestAsHex(key.getBytes()));
        }
        return signature;
    }


}
