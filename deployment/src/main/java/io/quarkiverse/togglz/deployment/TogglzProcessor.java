package io.quarkiverse.togglz.deployment;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Default;
import jakarta.inject.Singleton;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

import io.quarkiverse.togglz.runtime.FeatureManagerRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class TogglzProcessor {
    private static final String FEATURE = "togglz";

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
    SyntheticBeanBuildItem produceFeatureManager(final FeatureManagerRecorder featureManagerRecorder,
            final TogglzFeaturesBuildItem togglzFeaturesBuildItem) {
        // TODO find a way to inject StateRepository and UserProvider
        return SyntheticBeanBuildItem.configure(FeatureManager.class)
                .scope(Singleton.class)
                .qualifiers(AnnotationInstance.builder(Default.class).build())
                .createWith(featureManagerRecorder.createFeatureManager(
                        togglzFeaturesBuildItem.getFeatures(),
                        new InMemoryStateRepository(),
                        new NoOpUserProvider()))
                .unremovable()
                .done();
    }
}
