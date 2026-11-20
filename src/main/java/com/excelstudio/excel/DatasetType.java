package com.excelstudio.excel;

/**
 * Built-in practice dataset shapes.
 */
public enum DatasetType {
    EMPLOYEES("Employee records"),
    SALES("Sales transactions"),
    INVENTORY("Inventory records"),
    STUDENTS("Student records"),
    EXPENSES("Expenses"),
    ATTENDANCE("Attendance records"),
    CUSTOMERS("Customer records"),
    PRODUCTS("Product information");

    private final String displayName;

    DatasetType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
