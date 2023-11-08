package io.quarkiverse.togglz.deployment;

import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import jakarta.inject.Singleton;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;
import org.togglz.core.Feature;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import io.quarkiverse.togglz.runtime.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.undertow.deployment.FilterBuildItem;

class TogglzProcessor {
    private static final String FEATURE = "togglz";
    private static final Logger LOGGER = Logger.getLogger(TogglzProcessor.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem discoverFeatures(final ApplicationIndexBuildItem applicationIndexBuildItem,
            final FeatureRecorder featureRecorder) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final List<Class<? extends Feature>> features = applicationIndexBuildItem.getIndex().getAllKnownImplementors(
                DotName.createSimple(Feature.class))
                .stream()
                .filter(ClassInfo::isEnum)
                .map(classInfo -> {
                    try {
                        final Class<? extends Feature> feature;
                        feature = classLoader.loadClass(classInfo.name().toString())
                                .asSubclass(Feature.class);
                        return feature;
                    } catch (final ClassNotFoundException | ClassCastException e) {
                        throw new IllegalStateException("Should not be here", e);
                    }
                }).collect(Collectors.toList());
        return SyntheticBeanBuildItem.configure(FeaturesProvider.class)
                .scope(Singleton.class)
                .defaultBean()
                .createWith(featureRecorder.createFeaturesProvider(features))
                .unremovable()
                .done();
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem produceStateRepositoryResultBuildItem(final Capabilities capabilities,
            final StateRepositoryRecorder stateRepositoryRecorder) {
        if (capabilities.isPresent(Capability.AGROAL)) {
            LOGGER.infov("Using jdbc datasource state repository");
            return SyntheticBeanBuildItem.configure(StateRepository.class)
                    .scope(Singleton.class)
                    .defaultBean()
                    .createWith(stateRepositoryRecorder.createJDBCStateRepository())
                    .addInjectionPoint(ClassType.create(DataSource.class))
                    .unremovable()
                    .done();
        } else {
            LOGGER.infov("Using in memory state repository");
            return SyntheticBeanBuildItem.configure(StateRepository.class)
                    .scope(Singleton.class)
                    .defaultBean()
                    .createWith(stateRepositoryRecorder.createInMemoryStateRepository())
                    .unremovable()
                    .done();
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem produceUserProviderResultBuildItem(final Capabilities capabilities,
            final UserProviderRecorder userProviderRecorder) {
        if (capabilities.isPresent(Capability.SERVLET)) {
            LOGGER.infov("Using ServletUserProvider");
            return SyntheticBeanBuildItem.configure(UserProvider.class)
                    .scope(Singleton.class)
                    .defaultBean()
                    .createWith(userProviderRecorder.createServletUserProvider())
                    .unremovable()
                    .done();
        } else {
            LOGGER.infov("Using NoOpUserProvider");
            return SyntheticBeanBuildItem.configure(UserProvider.class)
                    .scope(Singleton.class)
                    .defaultBean()
                    .createWith(userProviderRecorder.createNoOpUserProvider())
                    .unremovable()
                    .done();
        }
    }

    @BuildStep
    void registerTogglzFilter(final Capabilities capabilities,
            final BuildProducer<FilterBuildItem> filterProducer) {
        if (capabilities.isPresent(Capability.SERVLET)) {
            // pour le test cf RolesAllowedServletTestCase dans le projet Quarkus ... je devrais tester qui je suis en fonction de la feature
            // cf. SmallRyeOpenTracingProcessor
            //            utiliser un wrapper et passer par Arc getInstance ... PUIS vireer @Startup
            //  final FilterBuildItem filterInfo = FilterBuildItem.builder("togglzFilter", QuarkusTogglzFilter.class.getName())
            //          .setAsyncSupported(true)
            //          .build();
            //  filterProducer.produce(filterInfo);
            putian le filter est auto decouvert ... et du coup le init est appelé automagicement !!! je vais devoir refaire une implemenation ... faire un V2 !
        }
    }

    @BuildStep
    public void producer(final BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {
        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(FeatureManagerProducer.class));
    }
}
