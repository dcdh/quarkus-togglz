package io.quarkiverse.togglz.runtime;

import java.util.function.Function;

import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;
import org.togglz.servlet.user.ServletUserProvider;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class UserProviderRecorder {
    public Function<SyntheticCreationalContext<UserProvider>, UserProvider> createNoOpUserProvider() {
        return (context) -> new NoOpUserProvider();
    }

    public Function<SyntheticCreationalContext<ServletUserProvider>, ServletUserProvider> createServletUserProvider() {
        //ok je vais devoir inclure le role featureAdmin ...
        // FCK TODO make it contribute : Optional configuration
        // regarder l'integration spring boot !!! il y a du filter à configurer !!!
        return (context) -> new ServletUserProvider("admin");
    }
}
