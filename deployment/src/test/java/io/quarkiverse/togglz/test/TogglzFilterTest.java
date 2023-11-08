package io.quarkiverse.togglz.test;

import static org.hamcrest.Matchers.is;

import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.security.test.utils.TestIdentityController;
import io.quarkus.security.test.utils.TestIdentityProvider;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

public class TogglzFilterTest {
    private static final String X_FEATURES = "X-Features";

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(QueryFeatureServlet.class,
                            BasicFeatures.class,
                            TestIdentityProvider.class,
                            TestIdentityController.class))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-undertow", Version.getVersion())));

    @BeforeAll
    public static void setupUsers() {
        TestIdentityController.resetRoles()
                .add("admin", "admin", "admin")
                .add("user", "user", "user");
    }

    @Test
    public void shouldBeActiveWhenFeatureIsInHeader() {
        RestAssured.given()
                .header(X_FEATURES, "featureOneActive")
                .get("/query-basic-feature")
                .then()
                .statusCode(200)
                .body(is("0"));
    }

    @Test
    public void shouldBeInActiveWhenFeatureIsNotInHeader() {
        RestAssured.given()
                .get("/query-basic-feature")
                .then()
                .statusCode(200)
                .body(is("0"));
    }

}
