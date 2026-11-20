package com.excelstudio.model;

/**
 * Types of checks a validator can perform against a saved workbook.
 */
public enum ValidationType {
    CELL_VALUE,
    CELL_FORMULA,
    CELL_EMPTY,
    CELL_BOLD,
    COLUMN_WIDTH,
    ROW_HEIGHT,
    SHEET_PRESENT,
    COLUMN_VALUES,
    CHART_PRESENT,
    WORKBOOK_READABLE
}
