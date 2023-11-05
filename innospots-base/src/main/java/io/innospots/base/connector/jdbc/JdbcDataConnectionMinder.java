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

package io.innospots.base.connector.jdbc;


import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.ds.hikari.HikariDSFactory;
import cn.hutool.setting.Setting;
import io.innospots.base.connector.credential.ConnectionCredential;
import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.jdbc.JdbcDataOperator;
import io.innospots.base.connector.schema.SchemaField;
import io.innospots.base.connector.schema.SchemaRegistry;
import io.innospots.base.connector.schema.SchemaRegistryType;
import io.innospots.base.exception.data.DataConnectionException;
import io.innospots.base.exception.data.DataSchemaException;
import io.innospots.base.model.field.FieldValueType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Raydian
 * @date 2021/1/31
 */
public class JdbcDataConnectionMinder extends BaseDataConnectionMinder {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDataConnectionMinder.class);

    public static final String MAX_POOL_SIZE = "5";
    public static final String SERVER_IP = "server_ip";
    public static final String DATABASE = "database";
    public static final String PORT = "port";
    public static final String JDBC_URL_PREFIX = "jdbcUrlPrefix";
    public static final String JDBC_URL_PARAM = "jdbcUrlParam";
    public static final String USERNAME = "user_name";
    public static final String PASSWORD = "db_password";
    public static final String DRIVER_CLASS_NAME = "driver_class";
    public static final String TABLE = "TABLE";
    public static final String VIEW = "VIEW";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String TABLE_TYPE = "TABLE_TYPE";
    public static final String[] TABLE_TYPES = new String[]{TABLE, VIEW};
    public static final String REMARKS = "REMARKS";
    public static final String COLUMN_NAME = "COLUMN_NAME";
    public static final String COLUMN_TYPE_NAME = "TYPE_NAME";

    protected DataSource dataSource;

    protected JdbcDataOperator dataOperator;

    public void open(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void open() {
        if (dataSource != null) {
            return;
        }
        logger.info("open datasource: {}", connectionCredential);
        this.dataSource = buildDataSource(connectionCredential, MAX_POOL_SIZE);
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        DataSource dataSource = buildDataSource(connectionCredential, "1");
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection == null) {
                return false;
            } else {
                if (StringUtils.isNotBlank(connection.getCatalog())) {
                    return true;
                } else {
                    throw DataConnectionException.buildException(this.getClass(), "The data source must specify a specific database");
                }
            }

        } catch (Exception e) {
            logger.error("Connection test failure ", e);
            throw DataConnectionException.buildException(this.getClass(), "Connection test error");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Connection testFailure, " + e.getMessage());
                }
            }
            close(dataSource);
        }
    }


    @Override
    public SchemaRegistry schemaRegistry(String tableName) {
        ResultSet resultSet = null;
        try (Connection connection = this.dataSource.getConnection()) {
            SchemaRegistry schemaRegistry = null;
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            resultSet = metaData.getTables(catalog,
                    connection.getSchema(), tableName, TABLE_TYPES);

            if (resultSet.next()) {
                schemaRegistry = buildSchemaRegistry(resultSet);
            }
            return schemaRegistry;
        } catch (SQLException e) {
            throw DataSchemaException.buildException(this.getClass(), "Get schemaTable failure", e);
        } finally {
            this.closeResultSet(resultSet);
        }
    }


    private List<SchemaField> schemaRegistryFields(Connection connection, String tableName) {
        List<SchemaField> schemaFields = new ArrayList<>();
        ResultSet resultSet = null;
        ResultSet pkResultSet = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String columnNamePattern = "%";
            resultSet = metaData.getColumns(catalog, connection.getSchema(), tableName, columnNamePattern);

            if (resultSet == null) {
                return Collections.emptyList();
            }

            while (resultSet.next()) {
                String remark = resultSet.getString(REMARKS);
                String columnName = resultSet.getString(COLUMN_NAME);
                String table = resultSet.getString(TABLE_NAME);
                String type = resultSet.getString(COLUMN_TYPE_NAME).toUpperCase();

                if (StringUtils.isEmpty(remark)) {
                    remark = columnName;
                }

                SchemaField schemaField = new SchemaField();
                schemaField.setRegistryCode(table);
                schemaField.setName(remark);
                schemaField.setCode(columnName);
                schemaField.setValueType(this.convertType(type));
                schemaFields.add(schemaField);
            }

            pkResultSet = metaData.getPrimaryKeys(catalog, connection.getSchema(), tableName);

            Set<String> pkSets = new HashSet<>();
            while (pkResultSet.next()) {
                String pkColName = pkResultSet.getString(COLUMN_NAME);
                pkSets.add(pkColName);
            }

            for (SchemaField schemaField : schemaFields) {
                if (pkSets.contains(schemaField.getCode())) {
                    schemaField.setPkey(true);
                }
            }

            return schemaFields;
        } catch (SQLException e) {
            throw DataSchemaException.buildException(this.getClass(), "Fill schemaTable failure", e);
        } finally {
            this.closeResultSet(resultSet);
            this.closeResultSet(pkResultSet);
        }
    }

    @Override
    public List<SchemaRegistry> schemaRegistries(boolean includeField) {

        ResultSet resultSet = null;
        try (Connection connection = this.dataSource.getConnection()) {
            List<SchemaRegistry> schemaRegistryList = new ArrayList<>();
            String schema = connection.getSchema();
            DatabaseMetaData metaData = connection.getMetaData();

            String catalog = connection.getCatalog();
            String tableNamePattern = "%";
            resultSet = metaData.getTables(catalog, schema, tableNamePattern, TABLE_TYPES);

            while (resultSet.next()) {
                SchemaRegistry schemaRegistry = buildSchemaRegistry(resultSet);
                if (includeField) {
                    schemaRegistry.setSchemaFields(schemaRegistryFields(connection, schemaRegistry.getName()));
                }
                schemaRegistryList.add(schemaRegistry);
            }

            return schemaRegistryList;
        } catch (SQLException e) {
            throw DataSchemaException.buildException(this.getClass(), "Get schemaTable failure", e);
        } finally {
            this.closeResultSet(resultSet);
        }
    }

    @Override
    public String schemaName() {
        return "jdbc";
    }

    private SchemaRegistry buildSchemaRegistry(ResultSet resultSet) throws SQLException {
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        String name = resultSet.getString(TABLE_NAME);
        String comment = resultSet.getString(REMARKS);
        String tableType = resultSet.getString(TABLE_TYPE);
        schemaRegistry.setCredentialKey(this.connectionCredential.getCredentialKey());
        schemaRegistry.setCode(name);
        schemaRegistry.setName(name);
        schemaRegistry.setDescription(StringUtils.isNotBlank(comment) ? comment : name);
        schemaRegistry.setRegistryType(TABLE.equals(tableType) ? SchemaRegistryType.TABLE : SchemaRegistryType.VIEW);
        return schemaRegistry;
    }


    @Override
    public IDataOperator buildOperator() {
        if (this.dataOperator == null) {
            this.dataOperator = new JdbcDataOperator(dataSource);
        }
        return dataOperator;
    }


    public static DataSource buildDataSource(ConnectionCredential connectionCredential, String maxPoolSize) {
        Map<String, Object> configs = connectionCredential.getConfig();
        Setting setting = new Setting();
        DataSource dataSource = null;
        String jdbcParam = connectionCredential.prop(JDBC_URL_PARAM) == null ?
                "allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&zeroDateTimeBehavior=CONVERT_TO_NULL" :
                connectionCredential.prop(JDBC_URL_PARAM);

//        HikariDataSource hikariDataSource = new HikariDataSource();

        String jdbcUrl = "" + configs.get(JDBC_URL_PREFIX) +
                configs.get(SERVER_IP) +
                ":" +
                configs.get(PORT) +
                "/" +
                configs.get(DATABASE) + "?" + jdbcParam;

        setting.set(DSFactory.KEY_ALIAS_URL[1], jdbcUrl);
        setting.set(DSFactory.KEY_ALIAS_DRIVER[1], String.valueOf(configs.get(DRIVER_CLASS_NAME)));
        setting.set(DSFactory.KEY_ALIAS_USER[1], String.valueOf(configs.get(USERNAME)));
        setting.set(DSFactory.KEY_ALIAS_PASSWORD[1], String.valueOf(configs.get(PASSWORD)));
        setting.set(DSFactory.KEY_CONN_PROPS[0], "true");
        setting.set(DSFactory.KEY_CONN_PROPS[1], "true");
        setting.set("maxPoolSize", maxPoolSize);
        setting.set("minIdle", "1");
        setting.set("connectionTestQuery", "select 1");
        configs.forEach((k, v) -> {
            setting.put(k, String.valueOf(v));
        });

        try (DSFactory dsFactory = HikariDSFactory.create(setting)) {
            dataSource = dsFactory.getDataSource();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

//        hikariDataSource.setJdbcUrl(jdbcUrl);
//        hikariDataSource.setJdbcUrl(String.valueOf(configs.get(JDBC_URL)));
//        hikariDataSource.setUsername(String.valueOf(configs.get(USERNAME)));
//        hikariDataSource.setPassword(String.valueOf(configs.get(PASSWORD)));
//        hikariDataSource.setDriverClassName();

        // parameter setting
//        hikariDataSource.setMaximumPoolSize(5);
//        hikariDataSource.setMinimumIdle(1);
//        hikariDataSource.setMaxLifetime(35000);
//        hikariDataSource.setIdleTimeout(10000);
//        hikariDataSource.setConnectionTimeout(25000);
//        hikariDataSource.setValidationTimeout(40000);
//        hikariDataSource.setConnectionTestQuery("select 1");

        // Get remarks configuration
//        hikariDataSource.addDataSourceProperty("remarks", "true");
        // Get table remarks configuration
//        hikariDataSource.addDataSourceProperty("useInformationSchema", "true");

        return dataSource;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        return null;
    }


    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw DataConnectionException.buildException(this.getClass(), "Close resultSet failure");
            }
        }
    }

    protected FieldValueType convertType(String type) {
        FieldValueType valueType = null;
        switch (type) {
            case "BIT":
            case "TINYINT":
            case "INT":
            case "INT2":
            case "INT4":
            case "INTEGER":
                valueType = FieldValueType.INTEGER;
                break;
            case "INT8":
            case "BIGINT":
                valueType = FieldValueType.LONG;
                break;
            case "DOUBLE":
            case "FLOAT":
                valueType = FieldValueType.DOUBLE;
                break;
            case "NUMERIC":
            case "DECIMAL":
                valueType = FieldValueType.CURRENCY;
                break;
            case "VARCHAR":
            case "CHARACTER":
            case "TEXT":
            case "LONGVARCHAR":
            case "CHAR":
                valueType = FieldValueType.STRING;
                break;
            case "TIME":
            case "TIMESTAMP":
                valueType = FieldValueType.TIMESTAMP;
                break;
            case "BOOLEAN":
            case "BOOL":
                valueType = FieldValueType.BOOLEAN;
                break;
            case "DATE":
            case "DATETIME":
                valueType = FieldValueType.DATE;
                break;
            default:
                valueType = FieldValueType.STRING;
        }

        return valueType;
    }

    @Override
    public void close() {
        try {
            if (dataSource != null) {
                close(dataSource);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void close(DataSource dataSource) {
        try {
            Method close = dataSource.getClass().getMethod("close");
            close.invoke(dataSource);
            logger.info("close connection, credential: {}",connectionCredential.getCredentialKey());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error(e.getMessage());
        }
    }

}
