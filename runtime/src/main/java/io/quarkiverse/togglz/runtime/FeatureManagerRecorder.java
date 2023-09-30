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
            final List<Class<? extends Feature>> enumTypes) {
        Objects.requireNonNull(enumTypes);
        return (context) -> {
            final StateRepository stateRepository = context.getInjectedReference(StateRepository.class);
            final UserProvider userProvider = context.getInjectedReference(UserProvider.class);
            final FeatureManager featureManager = new MultiEnumFeatureTogglzBootstrap(enumTypes, stateRepository, userProvider)
                    .createFeatureManager();
            StaticFeatureManagerProvider.setFeatureManager(featureManager);
            return featureManager;
        };
    }
}
