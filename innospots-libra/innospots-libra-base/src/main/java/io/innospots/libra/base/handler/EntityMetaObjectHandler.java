/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.libra.base.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.innospots.base.utils.CCH;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 自动数据库填充
 *
 * @author Smars
 * @date 2021/7/14
 */
@Slf4j
public class EntityMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {

        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createdBy", String.class, CCH.authUser());
        this.strictInsertFill(metaObject, "updatedBy", String.class, CCH.authUser());
        this.strictInsertFill(metaObject, "projectId", Integer.class, CCH.projectId());

        log.debug("insert fill, context:{}", CCH.contextInfo());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updatedBy", CCH.authUser(), metaObject);
        this.setFieldValByName("projectId", CCH.projectId(), metaObject);
        log.debug("update fill, context:{}", CCH.contextInfo());
    }
}
