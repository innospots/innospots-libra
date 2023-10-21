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


import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.operator.ISqlOperator;
import io.innospots.base.data.operator.jdbc.JdbcDataOperator;
import io.innospots.base.data.operator.jdbc.JdbcSqlOperator;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.data.schema.SchemaField;
import io.innospots.base.data.schema.SchemaRegistry;
import io.innospots.base.data.schema.SchemaRegistryType;
import io.innospots.base.exception.data.DataConnectionException;
import io.innospots.base.exception.data.DataSchemaException;
import io.innospots.base.model.field.FieldValueType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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

    public static final String SERVER_IP = "server_ip";
    public static final String DATABASE = "database";
    public static final String PORT = "port";
    public static final String JDBC_URL_PREFIX = "jdbcUrlPrefix";
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

    protected JdbcSqlOperator sqlOperator;

    public void open(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void open() {

        if (dataSource != null) {
            HikariPoolMXBean mxBean = ((HikariDataSource) dataSource).getHikariPoolMXBean();
            logger.info("active connections:{}, idle connections:{}, thread await connections:{}", mxBean.getActiveConnections(), mxBean.getIdleConnections(), mxBean.getThreadsAwaitingConnection());
            return;
        }
        this.dataSource = buildDataSource(this.connectionCredential);
    }

    @Override
    public Object test(ConnectionCredential connectionCredential) {
        Map<String, Object> configs = connectionCredential.getConfig();
        HikariDataSource hikariDataSource = new HikariDataSource();
        String jdbcUrl = "" + configs.get(JDBC_URL_PREFIX) +
                configs.get(SERVER_IP) +
                ":" +
                configs.get(PORT) +
                "/" +
                configs.get(DATABASE) +
                "?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8&zeroDateTimeBehavior=CONVERT_TO_NULL";

        hikariDataSource.setJdbcUrl(jdbcUrl);
        hikariDataSource.setUsername(String.valueOf(configs.get(USERNAME)));
        hikariDataSource.setPassword(String.valueOf(configs.get(PASSWORD)));
        hikariDataSource.setDriverClassName(String.valueOf(configs.get(DRIVER_CLASS_NAME)));
        hikariDataSource.setMaximumPoolSize(1);
        // TODO 其他参数暂时先用默认的

        Connection connection = null;
        try {
            connection = hikariDataSource.getConnection();
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
                    throw DataConnectionException.buildException(this.getClass(), "Connection close error");
                }
            }

        }
    }

    @Override
    public void close() {
        try {
            if (dataSource != null) {
                if (dataSource instanceof HikariDataSource) {
                    if (!((HikariDataSource) dataSource).isClosed()) {
                        ((HikariDataSource) dataSource).close();
                    }
                }
            }
        } catch (Exception e) {
            throw DataConnectionException.buildException(this.getClass(), "Connection close failure", e);
        }
    }


    @Override
    public SchemaRegistry schemaRegistry(String tableName) {
        ResultSet resultSet = null;
        try (Connection connection = this.dataSource.getConnection()) {
            SchemaRegistry schemaRegistry = new SchemaRegistry();
            schemaRegistry.setCredentialId(this.connectionCredential.getCredentialId());
            schemaRegistry.setName(tableName);
            schemaRegistry.setCode(tableName);

            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            resultSet = metaData.getTables(catalog,
                    connection.getSchema(), tableName, TABLE_TYPES);

            String type = null;
            String comment = null;
            while (resultSet.next()) {
                type = resultSet.getString(TABLE_TYPE);
                comment = resultSet.getString(REMARKS);
            }
            if (StringUtils.isBlank(type)) {
                return null;
            }

            schemaRegistry.setSchemaFields(schemaRegistryFields(connection, tableName));
            schemaRegistry.setDescription(comment);
            schemaRegistry.setRegistryType(SchemaRegistryType.valueOf(type));

//            this.fillSchemaTable(connection, schemaRegistry);
            return schemaRegistry;
        } catch (SQLException e) {
            throw DataSchemaException.buildException(this.getClass(), "Get schemaTable failure", e);
        } finally {
            this.closeResultSet(resultSet);
        }
    }


    @Override
    public List<SchemaField> schemaRegistryFields(String tableName) {
        List<SchemaField> schemaFields = Collections.emptyList();
        try (Connection connection = this.dataSource.getConnection()) {
            schemaFields = schemaRegistryFields(connection, tableName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return schemaFields;
    }

    public List<SchemaField> schemaRegistryFields(Connection connection, String tableName) {
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
        //  TODO calcite
        ResultSet resultSet = null;
        try (Connection connection = this.dataSource.getConnection()) {
            List<SchemaRegistry> schemaRegistryList = new ArrayList<>();
            String schema = connection.getSchema();
            DatabaseMetaData metaData = connection.getMetaData();

            String catalog = connection.getCatalog();
            String tableNamePattern = "%";
            resultSet = metaData.getTables(catalog, schema, tableNamePattern, TABLE_TYPES);

            while (resultSet.next()) {

                String name = resultSet.getString(TABLE_NAME);
                String comment = resultSet.getString(REMARKS);
                String tableType = resultSet.getString(TABLE_TYPE);

                SchemaRegistry schemaRegistry = new SchemaRegistry();
                schemaRegistry.setCredentialId(this.connectionCredential.getCredentialId());
                schemaRegistry.setCode(name);
                schemaRegistry.setName(name);
                schemaRegistry.setDescription(StringUtils.isNotBlank(comment) ? comment : name);
                schemaRegistry.setRegistryType(TABLE.equals(tableType) ? SchemaRegistryType.TABLE : SchemaRegistryType.VIEW);

                if (includeField) {
                    schemaRegistry.setSchemaFields(schemaRegistryFields(connection, name));
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
    public IOperator buildOperator() {
        return dataOperator();
    }


    public IDataOperator dataOperator() {
        if (this.dataOperator == null) {
            this.dataOperator = new JdbcDataOperator(dataSource);
        }
        return dataOperator;
    }

    public ISqlOperator sqlOperator() {
        if (this.sqlOperator == null) {
            this.sqlOperator = new JdbcSqlOperator(dataSource);
        }
        return sqlOperator;
    }

    protected DataSource buildDataSource(ConnectionCredential connectionCredential) {
        Map<String, Object> configs = this.connectionCredential.getConfig();

        HikariDataSource hikariDataSource = new HikariDataSource();

        String jdbcUrl = "" + configs.get(JDBC_URL_PREFIX) +
                configs.get(SERVER_IP) +
                ":" +
                configs.get(PORT) +
                "/" +
                configs.get(DATABASE) +
                // TODO
                "?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8&zeroDateTimeBehavior=CONVERT_TO_NULL";

        hikariDataSource.setJdbcUrl(jdbcUrl);
//        hikariDataSource.setJdbcUrl(String.valueOf(configs.get(JDBC_URL)));
        hikariDataSource.setUsername(String.valueOf(configs.get(USERNAME)));
        hikariDataSource.setPassword(String.valueOf(configs.get(PASSWORD)));
        hikariDataSource.setDriverClassName(String.valueOf(configs.get(DRIVER_CLASS_NAME)));

        // parameter setting
        hikariDataSource.setMaximumPoolSize(5);
        hikariDataSource.setMinimumIdle(1);
//        hikariDataSource.setMaxLifetime(35000);
//        hikariDataSource.setIdleTimeout(10000);
//        hikariDataSource.setConnectionTimeout(25000);
//        hikariDataSource.setValidationTimeout(40000);
        hikariDataSource.setConnectionTestQuery("select 1");

        // Get remarks configuration
        hikariDataSource.addDataSourceProperty("remarks", "true");
        // Get table remarks configuration
        hikariDataSource.addDataSourceProperty("useInformationSchema", "true");
        return hikariDataSource;
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
        // TODO 以下类型未转换
        //  FIELD_CODE
        //  YEAR_MONTH
        //  YEAR_MONTH_DATE
        //  MONTH_DATE
        //  DAY_OF_MONTH

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

}
