package io.quarkiverse.togglz.runtime;

import java.util.function.Function;

import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class UserProviderRecorder {
    public Function<SyntheticCreationalContext<UserProvider>, UserProvider> createUserProvider() {
        return (context) -> new NoOpUserProvider();
    }
}
