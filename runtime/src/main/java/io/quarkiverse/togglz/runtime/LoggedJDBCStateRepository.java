package io.quarkiverse.togglz.runtime;

import java.sql.Connection;

import org.jboss.logging.Logger;
import org.togglz.core.repository.jdbc.JDBCStateRepository;

public class LoggedJDBCStateRepository extends JDBCStateRepository {
    private static final Logger LOGGER = Logger.getLogger(LoggedJDBCStateRepository.class);

    public LoggedJDBCStateRepository(final Builder builder) {
        super(builder);
    }

    @Override
    protected void beforeSchemaMigration(final Connection connection) {
        LOGGER.infov("Before schema migration execution");
    }

    @Override
    protected void afterSchemaMigration(final Connection connection) {
        LOGGER.infov("Schema migration executed");
    }
}
