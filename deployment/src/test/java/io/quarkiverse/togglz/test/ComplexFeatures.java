package io.quarkiverse.togglz.test;

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;

public enum ComplexFeatures implements Feature {
    FEATURE1,
    @EnabledByDefault
    FEATURE2
}
