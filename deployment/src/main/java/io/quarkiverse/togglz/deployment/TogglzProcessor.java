package io.quarkiverse.togglz.deployment;

import io.quarkiverse.togglz.runtime.FeatureManagerRecorder;
import io.quarkiverse.togglz.runtime.StateRepositoryRecorder;
import io.quarkiverse.togglz.runtime.UserProviderRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import jakarta.inject.Singleton;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

class TogglzProcessor {
    private static final String FEATURE = "togglz";
    private static final Logger LOGGER = Logger.getLogger(TogglzProcessor.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void discoverFeatures(final ApplicationIndexBuildItem applicationIndexBuildItem,
                          final BuildProducer<TogglzFeaturesBuildItem> togglzFeaturesBuildItem) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final List<Class<? extends Feature>> features = applicationIndexBuildItem.getIndex().getAllKnownImplementors(
                        DotName.createSimple(Feature.class))
                .stream()
                .filter(ClassInfo::isEnum)
                .map(classInfo -> {
                    try {
                        final Class<? extends Feature> feature = classLoader.loadClass(classInfo.name().toString())
                                .asSubclass(Feature.class);
                        return feature;
                    } catch (final ClassNotFoundException | ClassCastException e) {
                        throw new IllegalStateException("Should not be here", e);
                    }
                })
                .collect(Collectors.toList());
        togglzFeaturesBuildItem.produce(new TogglzFeaturesBuildItem(features));
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
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem produceFeatureManager(final FeatureManagerRecorder featureManagerRecorder,
                                                 final TogglzFeaturesBuildItem togglzFeaturesBuildItem) {
        return SyntheticBeanBuildItem.configure(FeatureManager.class)
                .scope(Singleton.class)
                .defaultBean()
                .createWith(featureManagerRecorder.createFeatureManager(togglzFeaturesBuildItem.getFeatures()))
                .unremovable()
                .addInjectionPoint(ClassType.create(StateRepository.class))
                .addInjectionPoint(ClassType.create(UserProvider.class))
                .done();
    }
}
