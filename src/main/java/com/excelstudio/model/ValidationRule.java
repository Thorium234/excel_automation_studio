package com.excelstudio.model;

import java.util.Locale;
import java.util.Objects;

/**
 * A single objective check applied to a saved workbook.
 */
public final class ValidationRule {
    private final ValidationType type;
    private final String sheet;
    private final int row;
    private final int column;
    private final Object expected;
    private final String description;

    private ValidationRule(ValidationType type, String sheet, int row, int column,
                           Object expected, String description) {
        this.type = Objects.requireNonNull(type, "type");
        this.sheet = sheet;
        this.row = row;
        this.column = column;
        this.expected = expected;
        this.description = description == null ? "" : description;
    }

    public static ValidationRule cellValue(CellAddress address, Object expected) {
        return new ValidationRule(ValidationType.CELL_VALUE, address.sheet(), address.row(),
                address.column(), expected, "Cell " + address.a1() + " should contain " + expected);
    }

    public static ValidationRule cellFormula(CellAddress address, String formula) {
        return new ValidationRule(ValidationType.CELL_FORMULA, address.sheet(), address.row(),
                address.column(), formula, "Cell " + address.a1() + " should contain " + formula);
    }

    public static ValidationRule cellEmpty(CellAddress address) {
        return new ValidationRule(ValidationType.CELL_EMPTY, address.sheet(), address.row(),
                address.column(), null, "Cell " + address.a1() + " should be empty");
    }

    public static ValidationRule cellBold(CellAddress address) {
        return new ValidationRule(ValidationType.CELL_BOLD, address.sheet(), address.row(),
                address.column(), Boolean.TRUE, "Cell " + address.a1() + " should be bold");
    }

    public static ValidationRule columnWidth(String sheet, int column, double characters) {
        return new ValidationRule(ValidationType.COLUMN_WIDTH, sheet, -1, column, characters,
                "Column " + CellAddress.columnName(column) + " should be about " + characters
                        + " characters wide");
    }

    public static ValidationRule rowHeight(String sheet, int row, double points) {
        return new ValidationRule(ValidationType.ROW_HEIGHT, sheet, row, -1, points,
                "Row " + (row + 1) + " should be about " + points + " points high");
    }

    public static ValidationRule sheetPresent(String sheet) {
        return new ValidationRule(ValidationType.SHEET_PRESENT, sheet, -1, -1, sheet,
                "Worksheet '" + sheet + "' should exist");
    }

    public static ValidationRule columnValues(String sheet, int firstRow, int column,
                                              java.util.List<?> expectedValues) {
        return new ValidationRule(ValidationType.COLUMN_VALUES, sheet, firstRow, column,
                java.util.List.copyOf(expectedValues),
                "Cells " + CellAddress.columnName(column) + (firstRow + 1) + ":"
                        + CellAddress.columnName(column) + (firstRow + expectedValues.size())
                        + " should contain the expected order");
    }

    public static ValidationRule chartPresent(String sheet, int expectedCharts) {
        return new ValidationRule(ValidationType.CHART_PRESENT, sheet, -1, -1, expectedCharts,
                "Worksheet '" + sheet + "' should contain " + expectedCharts + " chart(s)");
    }

    public static ValidationRule workbookReadable() {
        return new ValidationRule(ValidationType.WORKBOOK_READABLE, null, -1, -1, Boolean.TRUE,
                "The saved workbook should be readable");
    }

    public ValidationType type() {
        return type;
    }

    public String sheet() {
        return sheet;
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    public Object expected() {
        return expected;
    }

    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return type.name().toLowerCase(Locale.ROOT) + ": " + description;
    }
}
