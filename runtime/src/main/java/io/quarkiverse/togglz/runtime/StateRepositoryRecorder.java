package io.quarkiverse.togglz.runtime;

import java.util.function.Function;

import javax.sql.DataSource;

import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.jdbc.JDBCStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class StateRepositoryRecorder {
    public Function<SyntheticCreationalContext<StateRepository>, StateRepository> createInMemoryStateRepository() {
        return (context) -> new InMemoryStateRepository();
    }

    public Function<SyntheticCreationalContext<StateRepository>, StateRepository> createJDBCStateRepository() {
        return (context) -> {
            final DataSource dataSource = context.getInjectedReference(DataSource.class);
            return new JDBCStateRepository.Builder(dataSource)
                    .usePostgresTextColumns(true)
                    .build();
        };
    }
}
