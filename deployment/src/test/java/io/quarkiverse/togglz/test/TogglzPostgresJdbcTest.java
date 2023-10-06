package io.quarkiverse.togglz.test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.togglz.core.manager.FeatureManager;

import io.agroal.api.AgroalDataSource;
import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusUnitTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TogglzPostgresJdbcTest {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(BasicFeatures.class)
                    .addClass(Column.class)
                    .addClass(ColumnName.class)
                    .addClass(ColumnType.class)
                    .addAsResource(new StringAsset(
                            "quarkus.datasource.db-kind=postgresql\n" +
                                    "quarkus.datasource.jdbc.max-size=16"),
                            "application.properties"))
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-agroal", Version.getVersion()),
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion())));

    @Inject
    AgroalDataSource defaultDataSource;

    @Inject
    FeatureManager featureManager;

    @Test
    @Order(1)
    public void shouldUseDefaultTogglzTableName() throws SQLException {
        final Connection connection = defaultDataSource.getConnection();
        final List<String> tablesName = new ArrayList<>();
        try (final PreparedStatement showTablesStatement = connection.prepareStatement(
                "SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';");
                final ResultSet tables = showTablesStatement.executeQuery()) {
            while (tables.next()) {
                tablesName.add(tables.getString("tablename"));
            }
        }
        assertAll(
                () -> assertFalse(tablesName.isEmpty()),
                () -> assertTrue(tablesName.stream().anyMatch("togglz"::equals)));
    }

    @Test
    @Order(2)
    public void shouldGenerateExpectedColumns() throws SQLException {
        final Connection connection = defaultDataSource.getConnection();
        try (final PreparedStatement showTablesStatement = connection.prepareStatement(
                "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'togglz';");
                final ResultSet togglzTableColumns = showTablesStatement.executeQuery()) {
            final List<Column> actualColumns = new ArrayList<>();
            while (togglzTableColumns.next()) {
                actualColumns.add(
                        new Column(
                                new ColumnName(togglzTableColumns.getString("column_name")),
                                new ColumnType(togglzTableColumns.getString("data_type"))));
            }
            assertIterableEquals(
                    List.of(
                            new Column(ColumnName.FEATURE_ENABLED, ColumnType.INTEGER),
                            new Column(ColumnName.FEATURE_NAME, ColumnType.TEXT),
                            new Column(ColumnName.STRATEGY_ID, ColumnType.TEXT),
                            new Column(ColumnName.STRATEGY_PARAMS, ColumnType.TEXT)),
                    actualColumns);
        }
    }

    @Test
    @Order(3)
    public void shouldEnableFeature() {
        featureManager.enable(BasicFeatures.FEATURE1);
        assertAll(
                () -> assertTrue(featureManager.isActive(BasicFeatures.FEATURE1)),
                () -> assertTrue(BasicFeatures.FEATURE1.isActive()));
    }

    @Test
    @Order(4)
    public void shouldDisableFeature() {
        featureManager.disable(BasicFeatures.FEATURE1);
        assertAll(
                () -> assertFalse(featureManager.isActive(BasicFeatures.FEATURE1)),
                () -> assertFalse(BasicFeatures.FEATURE1.isActive()));
    }
}
