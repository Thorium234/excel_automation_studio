package com.excelstudio.excel;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.TaskResult;
import com.excelstudio.model.TaskStatus;
import com.excelstudio.model.ValidationResult;
import com.excelstudio.model.ValidationResult.ValidationIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates task generation, execution and validation. Contains no JavaFX code.
 */
public final class ExcelTaskEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelTaskEngine.class);

    private final TaskGenerator generator;
    private final TaskExecutor executor;
    private final TaskValidator validator;
    private final Supplier<Instant> clock;

    public ExcelTaskEngine(TaskGenerator generator, TaskExecutor executor, TaskValidator validator) {
        this(generator, executor, validator, Instant::now);
    }

    public ExcelTaskEngine(TaskGenerator generator, TaskExecutor executor, TaskValidator validator,
                           Supplier<Instant> clock) {
        this.generator = Objects.requireNonNull(generator, "generator");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public ExcelTask nextTask(TaskCategory category, TaskDifficulty difficulty) {
        ExcelTask task = generator.generate(category, difficulty);
        LOGGER.info("Generated task {} ({}, {})", task.name(), category, difficulty);
        return task;
    }

    public TaskResult run(ExcelTask task, Path workbook) {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(workbook, "workbook");
        Instant startedAt = clock.get();

        try {
            executor.execute(task, workbook);
        } catch (TaskExecutionException e) {
            LOGGER.error("Task execution failed: {}", task.name(), e);
            return failed(task, startedAt, workbook, "Execution failed: " + e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected task failure: {}", task.name(), e);
            return failed(task, startedAt, workbook, "Unexpected failure: " + e.getMessage());
        }

        ValidationResult validation;
        try {
            validation = validator.validate(task, workbook);
        } catch (RuntimeException e) {
            LOGGER.error("Validation failed: {}", task.name(), e);
            return failed(task, startedAt, workbook, "Validation failed: " + e.getMessage());
        }

        Instant finishedAt = clock.get();
        TaskStatus status = validation.isPassed() ? TaskStatus.PASSED : TaskStatus.FAILED;
        LOGGER.info("Task {} finished with {}", task.name(), status);
        return TaskResult.of(task.id(), task.name(), status, startedAt, finishedAt, workbook,
                validation, null);
    }

    private TaskResult failed(ExcelTask task, Instant startedAt, Path workbook, String message) {
        ValidationResult validation = ValidationResult.failed(
                java.util.List.of(ValidationIssue.of(message)), 0);
        return TaskResult.of(task.id(), task.name(), TaskStatus.FAILED, startedAt, clock.get(),
                workbook, validation, message);
    }
}
