package io.quarkiverse.togglz.runtime;

import java.util.function.Function;

import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class StateRepositoryRecorder {
    public Function<SyntheticCreationalContext<StateRepository>, StateRepository> createStateRepository() {
        return (context) -> new InMemoryStateRepository();
    }
}
