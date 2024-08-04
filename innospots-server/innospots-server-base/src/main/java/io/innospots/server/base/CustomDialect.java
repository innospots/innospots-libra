package io.innospots.server.base;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.*;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.NullType;
import org.hibernate.type.descriptor.jdbc.*;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/4
 */
public class CustomDialect extends MySQLDialect {

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        this.registerColumnTypes(typeContributions, serviceRegistry);
        NationalizationSupport nationalizationSupport = this.getNationalizationSupport();
        JdbcTypeRegistry jdbcTypeRegistry = typeContributions.getTypeConfiguration().getJdbcTypeRegistry();
        if (nationalizationSupport == NationalizationSupport.EXPLICIT) {
            jdbcTypeRegistry.addDescriptor(NCharJdbcType.INSTANCE);
            jdbcTypeRegistry.addDescriptor(NVarcharJdbcType.INSTANCE);
            jdbcTypeRegistry.addDescriptor(LongNVarcharJdbcType.INSTANCE);
            jdbcTypeRegistry.addDescriptor(NClobJdbcType.DEFAULT);
        }

        if (this.useInputStreamToInsertBlob()) {
            jdbcTypeRegistry.addDescriptor(2005, ClobJdbcType.STREAM_BINDING);
        }

        if (this.getTimeZoneSupport() == TimeZoneSupport.NATIVE) {
            jdbcTypeRegistry.addDescriptor(TimestampUtcAsOffsetDateTimeJdbcType.INSTANCE);
            jdbcTypeRegistry.addDescriptor(TimeUtcAsOffsetTimeJdbcType.INSTANCE);
        } else {
            jdbcTypeRegistry.addDescriptor(TimestampUtcAsJdbcTimestampJdbcType.INSTANCE);
            jdbcTypeRegistry.addDescriptor(TimeUtcAsJdbcTimeJdbcType.INSTANCE);
        }

        if (this.supportsStandardArrays()) {
            jdbcTypeRegistry.addTypeConstructor(ArrayJdbcTypeConstructor.INSTANCE);
        }

        if (this.supportsMaterializedLobAccess()) {
            jdbcTypeRegistry.addDescriptor(3004, BlobJdbcType.MATERIALIZED);
            jdbcTypeRegistry.addDescriptor(3005, ClobJdbcType.MATERIALIZED);
            jdbcTypeRegistry.addDescriptor(3006, NClobJdbcType.MATERIALIZED);
        }
        jdbcTypeRegistry.addDescriptorIfAbsent(3001, MySQLCastingJsonJdbcType.INSTANCE);
        typeContributions.contributeJdbcType(NullJdbcType.INSTANCE);
        typeContributions.contributeType(new NullType(NullJdbcType.INSTANCE, typeContributions.getTypeConfiguration().getJavaTypeRegistry().getDescriptor(Object.class)));
//        jdbcTypeRegistry.addDescriptor(EnumJdbcType.INSTANCE);
//        jdbcTypeRegistry.addDescriptor(OrdinalEnumJdbcType.INSTANCE);
    }
}
