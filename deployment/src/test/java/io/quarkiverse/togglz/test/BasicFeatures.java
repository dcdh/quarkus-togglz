package io.quarkiverse.togglz.test;

import org.togglz.core.Feature;
import org.togglz.core.annotation.ActivationParameter;
import org.togglz.core.annotation.DefaultActivationStrategy;
import org.togglz.core.annotation.EnabledByDefault;

public enum BasicFeatures implements Feature {
    @DefaultActivationStrategy(id = "header", parameters = {
            @ActivationParameter(name = "X-Features", value = "featureOneActive")
    })
    FEATURE1,
    @EnabledByDefault
    FEATURE2
}
