package io.quarkiverse.togglz.runtime;

import java.util.List;
import java.util.function.Function;

import org.togglz.core.Feature;

import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class FeatureRecorder {
    public Function<SyntheticCreationalContext<FeaturesProvider>, FeaturesProvider> createFeaturesProvider(
            final List<Class<? extends Feature>> features) {
        return (context) -> (FeaturesProvider) () -> features;
    }
}
