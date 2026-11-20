package com.excelstudio.excel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable, validated table of practice data.
 */
public final class Dataset {
    private static final Set<Class<?>> SUPPORTED_TYPES = Set.of(
            String.class, Integer.class, Long.class, Double.class, Boolean.class, LocalDate.class);

    private final String name;
    private final List<String> columns;
    private final List<List<Object>> rows;

    public Dataset(String name, List<String> columns, List<List<Object>> rows) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Dataset name must not be blank");
        }
        this.name = name;
        this.columns = validateColumns(columns);
        this.rows = validateRows(rows, this.columns.size());
    }

    private static List<String> validateColumns(List<String> columns) {
        Objects.requireNonNull(columns, "columns");
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("A dataset needs at least one column");
        }
        List<String> copy = new ArrayList<>(columns.size());
        Set<String> seen = new HashSet<>();
        for (String column : columns) {
            if (column == null || column.isBlank()) {
                throw new IllegalArgumentException("Column names must not be blank");
            }
            String normalized = column.trim().toLowerCase(Locale.ROOT);
            if (!seen.add(normalized)) {
                throw new IllegalArgumentException("Duplicate column: " + column);
            }
            copy.add(column.trim());
        }
        return List.copyOf(copy);
    }

    private static List<List<Object>> validateRows(List<List<Object>> rows, int width) {
        Objects.requireNonNull(rows, "rows");
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("A dataset needs at least one row");
        }
        List<List<Object>> copy = new ArrayList<>(rows.size());
        for (List<Object> row : rows) {
            if (row == null || row.size() != width) {
                throw new IllegalArgumentException("Every row must have " + width + " values");
            }
            List<Object> values = new ArrayList<>(width);
            for (Object value : row) {
                if (value == null) {
                    throw new IllegalArgumentException("Dataset values must not be null");
                }
                if (!SUPPORTED_TYPES.contains(value.getClass())) {
                    throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
                }
                values.add(value);
            }
            copy.add(List.copyOf(values));
        }
        return List.copyOf(copy);
    }

    public String name() {
        return name;
    }

    public List<String> columns() {
        return columns;
    }

    public int columnCount() {
        return columns.size();
    }

    public int rowCount() {
        return rows.size();
    }

    public List<Object> row(int rowIndex) {
        return rows.get(rowIndex);
    }

    public List<List<Object>> rows() {
        return rows;
    }

    public Object value(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).get(columnIndex);
    }

    public int columnIndex(String column) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).equalsIgnoreCase(column)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown column: " + column);
    }

    public boolean hasColumn(String column) {
        return columns.stream().anyMatch(name -> name.equalsIgnoreCase(column));
    }

    public List<Object> columnValues(String column) {
        int index = columnIndex(column);
        List<Object> values = new ArrayList<>(rows.size());
        for (List<Object> row : rows) {
            values.add(row.get(index));
        }
        return values;
    }

    public List<Double> numericValues(String column) {
        List<Double> values = new ArrayList<>();
        for (Object value : columnValues(column)) {
            if (value instanceof Number number) {
                values.add(number.doubleValue());
            } else {
                throw new IllegalArgumentException("Column '" + column + "' is not numeric");
            }
        }
        return values;
    }

    public int firstNumericColumn() {
        for (int column = 0; column < columnCount(); column++) {
            if (rows.get(0).get(column) instanceof Number) {
                return column;
            }
        }
        throw new IllegalStateException("Dataset '" + name + "' has no numeric column");
    }

    public Dataset withRows(List<List<Object>> newRows) {
        return new Dataset(name, columns, newRows);
    }

    public String display(int rowIndex, int columnIndex) {
        Object value = value(rowIndex, columnIndex);
        if (value instanceof Double number) {
            return String.format(Locale.ROOT, "%.2f", number);
        }
        return String.valueOf(value);
    }

    public static List<Object> rowOf(Object... values) {
        List<Object> row = new ArrayList<>(values.length);
        Collections.addAll(row, values);
        return List.copyOf(row);
    }
}
