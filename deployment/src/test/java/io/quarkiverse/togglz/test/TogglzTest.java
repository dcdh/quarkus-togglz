package io.quarkiverse.togglz.test;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.togglz.core.manager.FeatureManager;

import io.quarkus.test.QuarkusUnitTest;

public class TogglzTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(BasicFeatures.class)
                    .addClass(ComplexFeatures.class));

    @Inject
    Instance<FeatureManager> featureManagers;

    @Inject
    FeatureManager featureManager;

    @Test
    public void shouldHaveOneInstance() {
        assertAll(
                () -> assertTrue(this.featureManagers.isResolvable()),
                () -> assertNotNull(this.featureManagers.get()));
    }

    @Test
    public void shouldHaveTwoFeatures() {
        assertEquals(4, this.featureManager.getFeatures().size());
    }

    @Test
    public void shouldEnable() {
        this.featureManager.enable(BasicFeatures.FEATURE1);
        assertTrue(this.featureManager.isActive(BasicFeatures.FEATURE1));
    }

    @Test
    public void shouldDisable() {
        this.featureManager.disable(BasicFeatures.FEATURE1);
        assertFalse(this.featureManager.isActive(BasicFeatures.FEATURE1));
    }

    @Test
    public void shouldGetFeatureActivityStatusFromFeatureManagerProvider() {
        assertDoesNotThrow(BasicFeatures.FEATURE1::isActive);
    }
}
