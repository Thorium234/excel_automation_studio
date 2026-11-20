package com.excelstudio.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Zero-based cell position inside a named worksheet.
 */
public record CellAddress(String sheet, int row, int column) {
    public static final int MAX_ROWS = 1_048_576;
    public static final int MAX_COLUMNS = 16_384;

    public CellAddress {
        if (sheet == null || sheet.isBlank()) {
            throw new IllegalArgumentException("Sheet name must not be blank");
        }
        if (row < 0 || row >= MAX_ROWS) {
            throw new IllegalArgumentException("Row out of range: " + row);
        }
        if (column < 0 || column >= MAX_COLUMNS) {
            throw new IllegalArgumentException("Column out of range: " + column);
        }
    }

    public static CellAddress of(String sheet, int row, int column) {
        return new CellAddress(sheet, row, column);
    }

    /**
     * Excel-style reference such as {@code B3} (row and column are 1-based there).
     */
    public String a1() {
        return columnName(column) + (row + 1);
    }

    public String qualifiedA1() {
        return "'" + sheet + "'!" + a1();
    }

    public static String columnName(int column) {
        if (column < 0 || column >= MAX_COLUMNS) {
            throw new IllegalArgumentException("Column out of range: " + column);
        }
        StringBuilder name = new StringBuilder();
        int value = column + 1;
        while (value > 0) {
            int remainder = (value - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            value = (value - 1) / 26;
        }
        return name.toString();
    }

    public static String range(String sheet, int firstRow, int firstColumn, int lastRow, int lastColumn) {
        Objects.requireNonNull(sheet, "sheet");
        return "'" + sheet + "'!" + columnName(firstColumn).toUpperCase(Locale.ROOT) + (firstRow + 1)
                + ":" + columnName(lastColumn) + (lastRow + 1);
    }
}
