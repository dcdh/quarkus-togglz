package io.quarkiverse.togglz.runtime;

import java.util.List;

import org.togglz.core.Feature;

public interface FeaturesProvider {
    List<Class<? extends Feature>> features();
}
