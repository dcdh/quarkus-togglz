package io.quarkiverse.togglz.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class TogglzResourceTest {
    @Test
    public void shouldGetStatus() {
        given()
                .when().get("/togglz/FEATURE2")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void shouldEnable() {
        given()
                .when().post("/togglz/FEATURE1/enable")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void shouldDisable() {
        given()
                .when().post("/togglz/FEATURE1/disable")
                .then()
                .statusCode(200)
                .body(is("false"));
    }
}
