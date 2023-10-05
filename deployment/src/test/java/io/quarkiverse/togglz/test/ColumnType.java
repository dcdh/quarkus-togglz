package io.quarkiverse.togglz.test;

import java.util.Objects;

public final class ColumnType {
    public static final ColumnType TEXT = new ColumnType("text");
    public static final ColumnType INTEGER = new ColumnType("integer");
    private final String type;

    public ColumnType(final String type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ColumnType that = (ColumnType) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "ColumnType{" +
                "type='" + type + '\'' +
                '}';
    }
}
