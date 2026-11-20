package com.excelstudio.excel;

import java.nio.file.Path;

import com.excelstudio.model.ValidationResult;

/**
 * Verifies that a saved workbook actually satisfies an exercise.
 */
@FunctionalInterface
public interface TaskValidator {
    ValidationResult validate(ExcelTask task, Path workbook);
}
