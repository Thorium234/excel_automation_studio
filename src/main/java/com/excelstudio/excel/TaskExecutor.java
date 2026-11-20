package com.excelstudio.excel;

import java.nio.file.Path;

/**
 * Performs the desktop and workbook work required by a task.
 */
@FunctionalInterface
public interface TaskExecutor {
    void execute(ExcelTask task, Path workbook) throws TaskExecutionException;
}
