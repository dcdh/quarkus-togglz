package io.quarkiverse.togglz.runtime;

import java.util.function.Function;

import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class StateRepositoryRecorder {
    private static final Logger LOGGER = Logger.getLogger(StateRepositoryRecorder.class);

    public Function<SyntheticCreationalContext<StateRepository>, StateRepository> createInMemoryStateRepository() {
        LOGGER.infov("Creating In Memory State Repository");
        return (context) -> new InMemoryStateRepository();
    }

    public Function<SyntheticCreationalContext<StateRepository>, StateRepository> createJDBCStateRepository() {
        return (context) -> {
            LOGGER.infov("Creating JDBC State Repository");
            final DataSource dataSource = context.getInjectedReference(DataSource.class);
            return new LoggedJDBCStateRepository(
                    JDBCStateRepository
                            .newBuilder(dataSource)
                            .createTable(true)
                            .usePostgresTextColumns(true));
        };
    }
}
