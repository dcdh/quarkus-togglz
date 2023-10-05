package io.quarkiverse.togglz.test;

import java.util.Objects;

public final class Column {
    private final ColumnName name;
    private final ColumnType type;

    public Column(final ColumnName name, final ColumnType type) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Column column = (Column) o;
        return Objects.equals(name, column.name) && Objects.equals(type, column.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Column{" +
                "name=" + name +
                ", type=" + type +
                '}';
    }
}
