package io.quarkiverse.togglz.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.togglz.core.manager.FeatureManager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TogglzMongoTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(BasicFeatures.class, Column.class, ColumnName.class, ColumnType.class))
            .setForcedDependencies(List.of(
                    Dependency.of("org.togglz", "togglz-mongodb", "4.4.0"), // TODO find a way to retrieve the version from the jar
                    Dependency.of("io.quarkus", "quarkus-mongodb-client", Version.getVersion())));

    @Inject
    MongoClient mongoClient;

    @Inject
    FeatureManager featureManager;

    @Test
    @Order(1)
    public void shouldEnableFeature() {
        featureManager.enable(BasicFeatures.FEATURE1);
        assertAll(
                () -> assertTrue(featureManager.isActive(BasicFeatures.FEATURE1)),
                () -> assertTrue(BasicFeatures.FEATURE1.isActive()));
    }

    @Test
    @Order(2)
    public void shouldUseDefaultTogglzCollectionName() {
        final List<String> collectionNames = new ArrayList<>();
        final MongoIterable<String> iterable = mongoClient.getDatabase("admin").listCollectionNames();
        try (final MongoCursor<String> iterator = iterable.iterator()) {
            iterator.forEachRemaining(collectionNames::add);
        }
        assertTrue(collectionNames.stream().anyMatch("togglz"::equals));
    }

    @Test
    @Order(3)
    public void shouldDisableFeature() {
        featureManager.disable(BasicFeatures.FEATURE1);
        assertAll(
                () -> assertFalse(featureManager.isActive(BasicFeatures.FEATURE1)),
                () -> assertFalse(BasicFeatures.FEATURE1.isActive()));
    }
}
