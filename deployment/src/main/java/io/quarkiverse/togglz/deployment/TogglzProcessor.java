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
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import com.mongodb.client.MongoClient;

import io.quarkiverse.togglz.runtime.FeatureManagerRecorder;
import io.quarkiverse.togglz.runtime.MongoStateRepositoryProducer;
import io.quarkiverse.togglz.runtime.StateRepositoryRecorder;
import io.quarkiverse.togglz.runtime.UserProviderRecorder;
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
    void produceStateRepositoryResultBuildItem(final Capabilities capabilities,
            final StateRepositoryRecorder stateRepositoryRecorder,
            final BuildProducer<SyntheticBeanBuildItem> syntheticBeanBuildItemProducer,
            final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        if (capabilities.isPresent(Capability.AGROAL)) {
            LOGGER.infov("Using jdbc datasource state repository");
            syntheticBeanBuildItemProducer.produce(SyntheticBeanBuildItem.configure(StateRepository.class)
                    .scope(Singleton.class)
                    .defaultBean()
                    .createWith(stateRepositoryRecorder.createJDBCStateRepository())
                    .addInjectionPoint(ClassType.create(DataSource.class))
                    .unremovable()
                    .done());
        } else if (capabilities.isPresent(Capability.MONGODB_CLIENT)) {
            LOGGER.infov("Using mongodb state repository");
            additionalBeanBuildItemProducer.produce(
                    AdditionalBeanBuildItem.builder()
                            .setUnremovable()
                            .addBeanClasses(MongoStateRepositoryProducer.class)
                            .build());
            /**
             * I cannot declare it this way below because at that time the MongoClient at injection point using the
             * default Qualifier is not available yet.
             * It maybe because of the MongoClientRecorder and createBlockingSyntheticBean
             * having this comment:
             * // pass the runtime config into the recorder to ensure that the DataSource related beans
             * // are created after runtime configuration has been set up
             * The integration tests are failing at startup while the TogglzMongoTest using the @RegisterExtension were working.
             * syntheticBeanBuildItemProducer.produce(SyntheticBeanBuildItem.configure(StateRepository.class)
             * .scope(Singleton.class)
             * .defaultBean()
             * .createWith(stateRepositoryRecorder.createMongoDBStateRepository())
             * .addInjectionPoint(ClassType.create(MongoClient.class))
             * .unremovable()
             * .done());
             */
        } else {
            LOGGER.infov("Using in memory state repository");
            syntheticBeanBuildItemProducer.produce(SyntheticBeanBuildItem.configure(StateRepository.class)
                    .scope(Singleton.class)
                    .defaultBean()
                    .createWith(stateRepositoryRecorder.createInMemoryStateRepository())
                    .unremovable()
                    .done());
        }
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem produceUserProviderResultBuildItem(final UserProviderRecorder userProviderRecorder) {
        return SyntheticBeanBuildItem.configure(UserProvider.class)
                .scope(Singleton.class)
                .defaultBean()
                .createWith(userProviderRecorder.createUserProvider())
                .unremovable()
                .done();
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
