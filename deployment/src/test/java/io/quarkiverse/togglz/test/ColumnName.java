package io.quarkiverse.togglz.test;

import java.util.Objects;

public final class ColumnName {
    public static final ColumnName FEATURE_ENABLED = new ColumnName("feature_enabled");
    public static final ColumnName FEATURE_NAME = new ColumnName("feature_name");
    public static final ColumnName STRATEGY_ID = new ColumnName("strategy_id");
    public static final ColumnName STRATEGY_PARAMS = new ColumnName("strategy_params");

    private final String name;

    public ColumnName(final String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ColumnName that = (ColumnName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ColumnName{" +
                "name='" + name + '\'' +
                '}';
    }
}
