package io.innospots.server.base;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.H2DurationIntervalSecondJdbcType;
import org.hibernate.dialect.NationalizationSupport;
import org.hibernate.dialect.TimeZoneSupport;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.jdbc.*;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

/**
 * @author Smars
 * @date 2024/8/13
 */
public class CustomH2Dialect extends H2Dialect {

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

        jdbcTypeRegistry = typeContributions.getTypeConfiguration().getJdbcTypeRegistry();
        jdbcTypeRegistry.addDescriptor(TimeUtcAsOffsetTimeJdbcType.INSTANCE);
        jdbcTypeRegistry.addDescriptor(TimestampUtcAsInstantJdbcType.INSTANCE);
        jdbcTypeRegistry.addDescriptorIfAbsent(UUIDJdbcType.INSTANCE);
        jdbcTypeRegistry.addDescriptorIfAbsent(H2DurationIntervalSecondJdbcType.INSTANCE);
        jdbcTypeRegistry.addDescriptorIfAbsent(H2FormatJsonJdbcType.INSTANCE);
//        jdbcTypeRegistry.addDescriptor(EnumJdbcType.INSTANCE);
//        jdbcTypeRegistry.addDescriptor(OrdinalEnumJdbcType.INSTANCE);
     }
}
