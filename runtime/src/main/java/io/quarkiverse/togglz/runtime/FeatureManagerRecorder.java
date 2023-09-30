package io.quarkiverse.togglz.runtime;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.togglz.core.Feature;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class FeatureManagerRecorder {
    public Function<SyntheticCreationalContext<FeatureManager>, FeatureManager> createFeatureManager(
            final List<Class<? extends Feature>> enumTypes,
            final StateRepository stateRepository,
            final UserProvider userProvider) {
        Objects.requireNonNull(enumTypes);
        Objects.requireNonNull(stateRepository);
        Objects.requireNonNull(userProvider);
        return (context) -> {
            final FeatureManager featureManager = new MultiEnumFeatureTogglzBootstrap(enumTypes, stateRepository, userProvider)
                    .createFeatureManager();
            StaticFeatureManagerProvider.setFeatureManager(featureManager);
            return featureManager;
        };
    }
}
