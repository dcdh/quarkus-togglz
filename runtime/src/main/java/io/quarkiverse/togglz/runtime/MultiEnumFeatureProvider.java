package io.quarkiverse.togglz.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

// https://gist.github.com/chkal/5619754
public final class MultiEnumFeatureProvider implements FeatureProvider {
    private final Set<? extends Feature> features;

    public MultiEnumFeatureProvider(final List<Class<? extends Feature>> enumTypes) {
        Objects.requireNonNull(enumTypes);
        this.features = enumTypes.stream()
                .flatMap(enumType -> Stream.of(enumType.getEnumConstants()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Feature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    @Override
    public FeatureMetaData getMetaData(final Feature feature) {
        Objects.requireNonNull(feature);
        return new EnumFeatureMetaData(getFeatureByName(feature.name()));
    }

    private Feature getFeatureByName(final String name) {
        Objects.requireNonNull(name);
        return features.stream()
                .filter(feature -> name.equals(feature.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown feature: " + name));
    }
}
