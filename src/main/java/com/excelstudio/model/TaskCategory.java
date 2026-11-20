package com.excelstudio.model;

/**
 * Practice categories supported by the Excel task engine.
 */
public enum TaskCategory {
    DATA_ENTRY("Data entry"),
    CELL_NAVIGATION("Cell navigation"),
    EDITING("Editing"),
    DELETION("Deletion"),
    FORMULAS("Formulas"),
    FORMATTING("Formatting"),
    COLUMN_MANAGEMENT("Column management"),
    ROW_MANAGEMENT("Row management"),
    SORTING("Sorting"),
    FILTERING("Filtering"),
    DATA_CLEANING("Data cleaning"),
    CHARTS("Charts"),
    WORKSHEETS("Worksheets"),
    WORKBOOK_MANAGEMENT("Workbook management");

    private final String displayName;

    TaskCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
