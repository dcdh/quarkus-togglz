package io.quarkiverse.togglz.deployment;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.togglz.core.Feature;

import io.quarkus.builder.item.SimpleBuildItem;

public final class TogglzFeaturesBuildItem extends SimpleBuildItem {

    private final List<Class<? extends Feature>> features;

    public TogglzFeaturesBuildItem(final List<Class<? extends Feature>> features) {
        this.features = Objects.requireNonNull(features);
    }

    public List<Class<? extends Feature>> getFeatures() {
        return Collections.unmodifiableList(features);
    }
}
