package io.quarkiverse.togglz.runtime;

import java.util.List;
import java.util.Objects;

import org.togglz.core.Feature;
import org.togglz.core.bootstrap.TogglzBootstrap;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.UserProvider;

public final class MultiEnumFeatureTogglzBootstrap implements TogglzBootstrap {

    private final List<Class<? extends Feature>> enumTypes;
    private final StateRepository stateRepository;
    private final UserProvider userProvider;

    public MultiEnumFeatureTogglzBootstrap(final List<Class<? extends Feature>> enumTypes,
            final StateRepository stateRepository,
            final UserProvider userProvider) {
        this.enumTypes = Objects.requireNonNull(enumTypes);
        this.stateRepository = Objects.requireNonNull(stateRepository);
        this.userProvider = Objects.requireNonNull(userProvider);
    }

    @Override
    public FeatureManager createFeatureManager() {
        final FeatureProvider featureProvider = new MultiEnumFeatureProvider(enumTypes);
        return new FeatureManagerBuilder()
                .featureProvider(featureProvider)
                .stateRepository(stateRepository)
                .userProvider(userProvider)
                .build();
    }
}
