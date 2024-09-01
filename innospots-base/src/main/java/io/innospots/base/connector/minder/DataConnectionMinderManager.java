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

package io.innospots.base.connector.minder;

import com.github.benmanes.caffeine.cache.*;
import io.innospots.base.connector.credential.buidler.IRegistryCredentialBuilder;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.connector.schema.reader.SingleSchemaRegistryReader;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.credential.reader.IConnectionCredentialReader;
import io.innospots.base.connector.meta.ConnectionMinderSchema;
import io.innospots.base.connector.meta.ConnectionMinderSchemaLoader;
import io.innospots.base.connector.meta.CredentialAuthOption;
import io.innospots.base.connector.schema.reader.ISchemaRegistryReader;
import io.innospots.base.exception.LoadConfigurationException;
import io.innospots.base.exception.data.DataConnectionException;
import io.innospots.base.utils.BeanContextAwareUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * manage the datasource connection instance
 * data connection minder interface, this manager will load these class that implement the IDataConnectionMinder interface
 *
 * @author Raydian
 * @date 2021/1/31
 */
public class DataConnectionMinderManager {

    private static final Logger logger = LoggerFactory.getLogger(DataConnectionMinderManager.class);

    private final LoadingCache<String, IDataConnectionMinder> connectionPoolCache;
    private final Cache<String, IDataConnectionMinder> registryMinderCache;


    private final IConnectionCredentialReader connectionCredentialReader;

    private final ISchemaRegistryReader dataSchemaReader;

    public DataConnectionMinderManager(
            IConnectionCredentialReader connectionCredentialReader,
            ISchemaRegistryReader dataSchemaReader,
            int cacheTimeoutSecond) {
        this.connectionCredentialReader = connectionCredentialReader;
        this.dataSchemaReader = dataSchemaReader;
        connectionPoolCache = build(cacheTimeoutSecond);
        registryMinderCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheTimeoutSecond, TimeUnit.SECONDS)
                .build();
    }

    public static IDataConnectionMinder getCredentialMinder(String credentialKey) {
        return BeanContextAwareUtils.getBean(DataConnectionMinderManager.class).getMinder(credentialKey);
    }

    public static IQueueConnectionMinder getCredentialQueueMinder(String credentialKey) {
        return (IQueueConnectionMinder) BeanContextAwareUtils.getBean(DataConnectionMinderManager.class).getMinder(credentialKey);
    }

    public static IDataConnectionMinder newMinderInstance(String connectorName,String authOption){
        try {
            ConnectionMinderSchema minderSchema = ConnectionMinderSchemaLoader.getConnectionMinderSchema(connectorName);
            CredentialAuthOption config = minderSchema.getAuthOptions().stream().filter(f -> Objects.equals(authOption, f.getCode())).findFirst()
                    .orElseThrow(() -> LoadConfigurationException.buildException(ConnectionMinderSchemaLoader.class, "dataConnectionMinder newInstance failed, configCode invalid."));
            return (IDataConnectionMinder) Class.forName(config.getMinder()).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static IDataConnectionMinder newMinderInstance(ConnectionCredential connectionCredential) {
        return newMinderInstance(connectionCredential.getConnectorName(), connectionCredential.getAuthOption());
    }

    public static Object testConnection(ConnectionCredential connectionCredential) {
        IDataConnectionMinder dataConnectionMinder = newMinderInstance(connectionCredential);
        if (dataConnectionMinder != null) {
            return dataConnectionMinder.testConnect(connectionCredential);
        }
        return false;
    }

    public static Object fetchSample(ConnectionCredential connectionCredential) {
        CredentialAuthOption formConfig = ConnectionMinderSchemaLoader.getCredentialFormConfig(connectionCredential.getConnectorName(), connectionCredential.getAuthOption());
        if (formConfig == null) {
            return false;
        }
        IDataConnectionMinder dataConnectionMinder = newMinderInstance(connectionCredential);
        if (dataConnectionMinder != null) {
            dataConnectionMinder.open();
            return dataConnectionMinder.fetchSample(connectionCredential, null);
        }
        return null;
    }

    public Object fetchSample(String credentialKey, String tableName) {
        IDataConnectionMinder minder = this.getMinder(credentialKey);
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialKey);
        return minder.fetchSample(connectionCredential, tableName);
    }

    public PageBody<Map<String, Object>> fetchSamples(String credentialKey, Map<String, Object> config) {
        IDataConnectionMinder minder = this.getMinder(credentialKey);
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialKey);
        return minder.fetchSamples(connectionCredential, config);
    }

    private IDataConnectionMinder buildMinder(ConnectionCredential connectionCredential) {
        IDataConnectionMinder dataConnectionMinder = connectionPoolCache.getIfPresent(connectionCredential.key());
        if (dataConnectionMinder != null) {
            dataConnectionMinder.open();
            return dataConnectionMinder;
        }

        try {
            dataConnectionMinder = newMinderInstance(connectionCredential);
            if (dataConnectionMinder != null) {
                dataConnectionMinder.initialize(dataSchemaReader, connectionCredential);
//                connectionPoolCache.put(connectionCredential.key(), dataConnectionMinder);
                dataConnectionMinder.open();

                if (logger.isDebugEnabled()) {
                    logger.debug("register datasource:{}", connectionCredential);
                }
            } else {
                logger.error("not find connectionCredential, key:{}, authOption:{} ", connectionCredential.getCredentialKey(), connectionCredential.getAuthOption());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (dataConnectionMinder == null) {
            throw DataConnectionException.buildException(this.getClass(), "not find connectionCredential type:" + connectionCredential.getAuthOption());
        }

        return dataConnectionMinder;
    }

    public void unregister(String credentialKey) {
        connectionPoolCache.invalidate(credentialKey);
    }

    public IQueueConnectionMinder getQueueMinder(String credentialKey) {
        IDataConnectionMinder dataConnectionMinder = getMinder(credentialKey);
        if (dataConnectionMinder instanceof IQueueConnectionMinder) {
            return (IQueueConnectionMinder) dataConnectionMinder;
        }
        return null;
    }

    public  IDataConnectionMinder getMinder(String credentialKey) {
        return connectionPoolCache.get(credentialKey);
//        return connectionPoolCache.getIfPresent(credentialKey);
    }

    public IDataConnectionMinder getMinder(SchemaRegistry schemaRegistry) {
        if(schemaRegistry.getCredentialKey()!=null){
            return this.getMinder(schemaRegistry.getCredentialKey());
        }

        IDataConnectionMinder dataConnectionMinder = registryMinderCache.getIfPresent(schemaRegistry.getRegistryId());
        if(dataConnectionMinder != null){
            return dataConnectionMinder;
        }

        dataConnectionMinder = newMinderInstance(schemaRegistry.getConnectorName(),schemaRegistry.getAuthOption());
        if(dataConnectionMinder == null){
            return null;
        }
        IRegistryCredentialBuilder credentialBuilder = dataConnectionMinder.registryCredentialBuilder();
        ConnectionCredential connectionCredential = credentialBuilder.buildBySchemaRegistry(schemaRegistry);
        dataConnectionMinder.initialize(new SingleSchemaRegistryReader(schemaRegistry), connectionCredential);
        registryMinderCache.put(schemaRegistry.getRegistryId(), dataConnectionMinder);
        return dataConnectionMinder;
    }


    public void close() {
        if (connectionPoolCache != null) {
            connectionPoolCache.invalidateAll();
        }
    }

    private LoadingCache<String, IDataConnectionMinder> build(int timeoutSecond) {

        return Caffeine.newBuilder()
                .removalListener((RemovalListener<String, IDataConnectionMinder>) (s, dataConnectionMinder, removalCause) -> {
                    logger.warn("dataConnection is expired, close the data connection,key:{}", s);
                    if (dataConnectionMinder != null) {
                        dataConnectionMinder.close();
                    }
                })
                .expireAfterAccess(timeoutSecond, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public @Nullable IDataConnectionMinder load(@NonNull String credentialKey) throws Exception {
                        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialKey);
                        if (connectionCredential == null) {
                            logger.error("credential don't exist, {}", credentialKey);
                            return null;
                        }
                        return buildMinder(connectionCredential);
                    }
                });
    }

}
