package io.quarkiverse.togglz.runtime;

import java.util.function.Function;

import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;
import org.togglz.servlet.user.ServletUserProvider;

@Recorder
public class UserProviderRecorder {
    public Function<SyntheticCreationalContext<UserProvider>, UserProvider> createNoOpUserProvider() {
        return (context) -> new NoOpUserProvider();
    }

    public Function<SyntheticCreationalContext<ServletUserProvider>, ServletUserProvider> createServletUserProvider() {
        return (context) -> new ServletUserProvider("admin");// FCK TODO make it contribute : Optional configuration
    }
}
