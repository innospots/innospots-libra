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

package io.innospots.base.data.minder;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.data.schema.config.ConnectionMinderSchema;
import io.innospots.base.data.schema.config.ConnectionMinderSchemaLoader;
import io.innospots.base.data.schema.config.CredentialFormConfig;
import io.innospots.base.data.schema.reader.IConnectionCredentialReader;
import io.innospots.base.data.schema.reader.ISchemaRegistryReader;
import io.innospots.base.exception.LoadConfigurationException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.data.DataConnectionException;
import io.innospots.base.model.PageBody;
import io.innospots.base.utils.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * manage the datasource connection instance
 * data connection use spi interface, this manager will load these class that implement the IDataConnectionMinder interface
 *
 * @author Raydian
 * @date 2021/1/31
 */
public class DataConnectionMinderManager {

    private static final Logger logger = LoggerFactory.getLogger(DataConnectionMinderManager.class);

    private Cache<String, IDataConnectionMinder> connectionPoolCache;

    private IConnectionCredentialReader connectionCredentialReader;

    private ISchemaRegistryReader dataSchemaReader;

    public DataConnectionMinderManager(
            IConnectionCredentialReader connectionCredentialReader,
            ISchemaRegistryReader dataSchemaReader,
            int cacheTimeoutSecond) {
        this.connectionCredentialReader = connectionCredentialReader;
        this.dataSchemaReader = dataSchemaReader;
        connectionPoolCache = build(cacheTimeoutSecond);
    }

    public static IDataConnectionMinder getCredentialMinder(Integer credentialId) {
        return ApplicationContextUtils.getBean(DataConnectionMinderManager.class).getMinder(credentialId);
    }

    public static IQueueConnectionMinder getCredentialQueueMinder(Integer credentialId) {
        return (IQueueConnectionMinder) ApplicationContextUtils.getBean(DataConnectionMinderManager.class).getMinder(credentialId);
    }

    public static IDataConnectionMinder newInstanceByConnectorNameAndConfigCode(String connectorName, String configCode) {
        try {
            ConnectionMinderSchema minderSchema = ConnectionMinderSchemaLoader.getConnectionMinderSchema(connectorName);
            if (minderSchema == null) {
                return null;
            }
            CredentialFormConfig config = minderSchema.getConfigs().stream().filter(f -> configCode.equals(f.getCode())).findFirst()
                    .orElseThrow(() -> LoadConfigurationException.buildException(ConnectionMinderSchemaLoader.class, "dataConnectionMinder newInstance failed, configCode invalid."));

            return (IDataConnectionMinder) Class.forName(config.getMinder()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static Object testConnection(ConnectionCredential connectionCredential) {
        IDataConnectionMinder dataConnectionMinder =  newInstanceByConnectorNameAndConfigCode(connectionCredential.getConnectorName(),connectionCredential.getConfigCode());
        if (dataConnectionMinder != null) {
            return dataConnectionMinder.test(connectionCredential);
        }
        return false;
    }

    public static Object fetchSample(ConnectionCredential connectionCredential) {
        CredentialFormConfig formConfig = ConnectionMinderSchemaLoader.getCredentialFormConfig(connectionCredential.getConnectorName(), connectionCredential.getConfigCode());
        if (formConfig == null) {
            return false;
        }
        IDataConnectionMinder dataConnectionMinder = newInstanceByConnectorNameAndConfigCode(connectionCredential.getConnectorName(),connectionCredential.getConfigCode());
        if (dataConnectionMinder != null) {
            dataConnectionMinder.open();
            return dataConnectionMinder.fetchSample(connectionCredential, null);
        }
        return null;
    }

    public Object fetchSample(Integer credentialId, String tableName) {
        IDataConnectionMinder minder = this.getMinder(credentialId);
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialId);
        return minder.fetchSample(connectionCredential, tableName);
    }

    public PageBody<Map<String, Object>> fetchSamples(Integer credentialId, SchemaRegistry schemaRegistry, int page, int size) {
        IDataConnectionMinder minder = this.getMinder(credentialId);
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialId);
        return minder.fetchSamples(connectionCredential, schemaRegistry, page, size);
    }

    private IDataConnectionMinder getMinder(ConnectionCredential connectionCredential) {
        IDataConnectionMinder dataConnectionMinder = connectionPoolCache.getIfPresent(connectionCredential.key());
        if (dataConnectionMinder != null) {
            dataConnectionMinder.open();
            return dataConnectionMinder;
        }

        try {
            dataConnectionMinder = newInstanceByConnectorNameAndConfigCode(connectionCredential.getConnectorName(),connectionCredential.getConfigCode());
            if (dataConnectionMinder != null) {
                dataConnectionMinder.initialize(dataSchemaReader, connectionCredential);
                connectionPoolCache.put(connectionCredential.key(), dataConnectionMinder);
                dataConnectionMinder.open();

                if (logger.isDebugEnabled()) {
                    logger.debug("register datasource:{}", connectionCredential);
                }
            } else {
                logger.error("not find connectionCredential, id:{}, type:{} ", connectionCredential.getCredentialId(), connectionCredential.getConfigCode());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (dataConnectionMinder == null) {
            throw DataConnectionException.buildException(this.getClass(), "not find connectionCredential type:" + connectionCredential.getConfigCode());
        }

        return dataConnectionMinder;
    }

    public void unregister(Integer credentialId) {
        connectionPoolCache.invalidate(key(credentialId));
    }

    public IDataConnectionMinder getMinder(String credentialCode) {
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialCode);
        if (connectionCredential != null) {
            return getMinder(connectionCredential.getCredentialId());
        }
        return null;
    }

    public IQueueConnectionMinder getQueueMinder(Integer credentialId){
        IDataConnectionMinder dataConnectionMinder = getMinder(credentialId);
        if(dataConnectionMinder instanceof IQueueConnectionMinder){
            return (IQueueConnectionMinder) dataConnectionMinder;
        }
        return null;
    }

    public IDataConnectionMinder getMinder(Integer credentialId) {
        IDataConnectionMinder minder =
                connectionPoolCache.getIfPresent(key(credentialId));
        if (minder == null) {
            ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(credentialId);
            if (connectionCredential == null) {
                logger.error("credential config don't exist, {}", credentialId);
                throw ResourceException.buildNotExistException(this.getClass(), "credential config don't exist, credentialId: " + credentialId);
            }
            minder = getMinder(connectionCredential);
        }

        return minder;
    }

    private String key(Integer credentialId) {
        return String.valueOf(credentialId);
    }

    public void close() {
        if (connectionPoolCache != null) {
            connectionPoolCache.invalidateAll();
        }
    }

    private Cache<String, IDataConnectionMinder> build(int timeoutSecond) {

        return Caffeine.newBuilder()
                .removalListener((RemovalListener<String, IDataConnectionMinder>) (s, dataConnectionMinder, removalCause) -> {
                    logger.warn("dataConnection is expired, close the data connection,key:{}", s);
                    if (dataConnectionMinder != null) {
                        dataConnectionMinder.close();
                    }
                }).expireAfterAccess(timeoutSecond, TimeUnit.SECONDS).build();
    }

}
