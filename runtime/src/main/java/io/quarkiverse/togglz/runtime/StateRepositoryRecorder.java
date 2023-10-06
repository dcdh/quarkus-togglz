package io.quarkiverse.togglz.runtime;

import java.util.Optional;
import java.util.function.Function;

import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.mongodb.MongoStateRepository;

import com.mongodb.client.MongoClient;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class StateRepositoryRecorder {
    private static final Logger LOGGER = Logger.getLogger(StateRepositoryRecorder.class);

    public Function<SyntheticCreationalContext<StateRepository>, StateRepository> createInMemoryStateRepository() {
        return (context) -> {
            LOGGER.infov("Creating In Memory State Repository");
            return new InMemoryStateRepository();
        };
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

    public Function<SyntheticCreationalContext<StateRepository>, StateRepository> createMongoDBStateRepository() {
        return (context) -> {
            final MongoClient mongoClient = context.getInjectedReference(MongoClient.class);
            final String dbName = Optional.ofNullable(mongoClient.listDatabaseNames().first())
                    .orElseThrow(() -> new IllegalStateException("Should not be here"));
            LOGGER.infov("Creating MongoDB State Repository using dbname {0}", dbName);
            return MongoStateRepository.newBuilder(mongoClient, dbName)
                    .build();
        };
    }
}
