package com.excelstudio.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Immutable outcome of a single exercise run.
 */
public final class TaskResult {
    private final String taskId;
    private final String taskName;
    private final TaskStatus status;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final Path workbook;
    private final ValidationResult validation;
    private final String error;

    private TaskResult(String taskId, String taskName, TaskStatus status, Instant startedAt,
                       Instant finishedAt, Path workbook, ValidationResult validation, String error) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.workbook = workbook;
        this.validation = validation;
        this.error = error;
    }

    public static TaskResult of(String taskId, String taskName, TaskStatus status, Instant startedAt,
                                Instant finishedAt, Path workbook, ValidationResult validation, String error) {
        return new TaskResult(taskId, taskName, status, startedAt, finishedAt, workbook, validation, error);
    }

    public String taskId() {
        return taskId;
    }

    public String taskName() {
        return taskName;
    }

    public TaskStatus status() {
        return status;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }

    public Duration duration() {
        return Duration.between(startedAt, finishedAt);
    }

    public Path workbook() {
        return workbook;
    }

    public ValidationResult validation() {
        return validation;
    }

    public String error() {
        return error;
    }

    public boolean succeeded() {
        return status == TaskStatus.PASSED;
    }
}
