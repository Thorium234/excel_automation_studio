package com.excelstudio.model;

/**
 * Difficulty of a generated exercise.
 */
public enum TaskDifficulty {
    BASIC("Basic", 8),
    INTERMEDIATE("Intermediate", 12),
    ADVANCED("Advanced", 16);

    private final String displayName;
    private final int datasetRows;

    TaskDifficulty(String displayName, int datasetRows) {
        this.displayName = displayName;
        this.datasetRows = datasetRows;
    }

    public String displayName() {
        return displayName;
    }

    public int datasetRows() {
        return datasetRows;
    }
}
