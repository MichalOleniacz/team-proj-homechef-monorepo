package org.homechef.core.config;

import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.util.List;

/**
 * JDBC configuration for custom type converters.
 * Handles PostgreSQL-specific types like JSONB.
 */
@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Override
    protected List<?> userConverters() {
        return List.of(new PGobjectToStringConverter());
    }

    /**
     * Converter for reading PostgreSQL JSONB (PGobject) as String.
     */
    @ReadingConverter
    static class PGobjectToStringConverter implements Converter<PGobject, String> {
        @Override
        public String convert(PGobject source) {
            return source.getValue();
        }
    }
}
