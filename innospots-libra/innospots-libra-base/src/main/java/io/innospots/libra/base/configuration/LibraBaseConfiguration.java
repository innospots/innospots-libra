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

package io.innospots.libra.base.configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.google.common.collect.ImmutableList;
import io.innospots.base.store.CacheStoreManager;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.dao.handler.EntityMetaObjectHandler;
import io.innospots.libra.base.operator.SystemTempCacheOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Alfred
 * @date 2022/2/10
 */
@Configuration
@EntityScan(basePackages = {"io.innospots.libra.base.entity","io.innospots.libra.base.category"})
@MapperScan(basePackages = {"io.innospots.libra.base.dao","io.innospots.libra.base.category"})
@EnableConfigurationProperties({AuthProperties.class})
public class LibraBaseConfiguration {

    private final static List<String> IGNORE_TABLES = ImmutableList.of(
            "flow_execution",
            "flow_execution_node",
            "flow_execution_context",
            "i18n_currency",
            "i18n_dictionary",
            "i18n_language",
            "i18n_trans_message",
            "sys_avatar_resource",
            "sys_function",
            "sys_login_log",
            "sys_organization",
            "sys_org_user",
            "sys_service_registry",
            "sys_user",
            "sys_user_role",
            "ext_definition",
            "app_flow_template",
            "app_node_definition",
            "app_node_group",
            "app_node_group_node",
            "sys_todo_task",
            "sys_todo_task_comment",
            "sys_todo_task_tag",
            "sys_task_execution"
    );

    /**
     * mybatis-plus Paging plug-in
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // first add TenantLineInnerInterceptor and then add PaginationInnerInterceptor
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                return new LongValue(CCH.projectId());
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return ignoreTableCondition(tableName);
            }

            @Override
            public String getTenantIdColumn() {
                return "project_id";
            }
        }));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    @Bean
    public EntityMetaObjectHandler metaObjectHandler() {
        return new EntityMetaObjectHandler();
    }

    // TODO 后续规范表名的忽略规则
    private boolean ignoreTableCondition(String tableName) {
        long count = IGNORE_TABLES.stream().filter(data -> data.equals(tableName)).count();
        return count > 0;
    }

    @Bean
    public SystemTempCacheOperator systemTempCacheOperator() {
        SystemTempCacheOperator cacheOperator =  new SystemTempCacheOperator();
        CacheStoreManager.set(cacheOperator);
        return cacheOperator;
    }
}
