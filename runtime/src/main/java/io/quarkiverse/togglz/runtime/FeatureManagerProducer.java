package io.quarkiverse.togglz.runtime;

import java.util.List;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.jboss.logging.Logger;
import org.togglz.core.Feature;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;

@Singleton
public class FeatureManagerProducer {
    private static final Logger LOGGER = Logger.getLogger(FeatureManagerProducer.class);

    @Produces
    @Singleton
    @Unremovable
    @Startup
    public FeatureManager produceFeatureManager(final FeaturesProvider featuresProvider,
            final StateRepository stateRepository,
            final UserProvider userProvider) {
        final List<Class<? extends Feature>> enumTypes = featuresProvider.features();
        final FeatureManager featureManager = new MultiEnumFeatureTogglzBootstrap(enumTypes, stateRepository, userProvider)
                .createFeatureManager();
        StaticFeatureManagerProvider.setFeatureManager(featureManager);
        return featureManager;
    }

    //mais non c'est le deployment undertow qui est effectué avant la creation du bean !!!
    void startup(@Observes StartupEvent event, final FeatureManager featureManager) {
        LOGGER.infov("Load featureManager");
    }
}
