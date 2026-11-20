package com.excelstudio.excel;

/**
 * Raised when a task could not be carried out. A failed execution is never treated
 * as a completed exercise.
 */
public class TaskExecutionException extends Exception {
    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
