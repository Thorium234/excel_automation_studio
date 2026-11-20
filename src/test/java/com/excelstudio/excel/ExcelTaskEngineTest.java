package com.excelstudio.excel;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.TaskResult;
import com.excelstudio.model.TaskStatus;
import com.excelstudio.model.ValidationResult;
import com.excelstudio.model.ValidationResult.ValidationIssue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelTaskEngineTest {

    private final ExcelTask task = new TaskGenerator(new Random(1))
            .generate(TaskCategory.DATA_ENTRY, TaskDifficulty.BASIC);

    @Test
    void passesOnlyWhenValidationPasses() {
        List<String> executed = new ArrayList<>();
        ExcelTaskEngine engine = new ExcelTaskEngine(
                new TaskGenerator(new Random(1)),
                (generated, workbook) -> executed.add(workbook.toString()),
                (generated, workbook) -> ValidationResult.passed(generated.rules().size()));

        TaskResult result = engine.run(task, Path.of("workbook.xlsx"));

        assertEquals(TaskStatus.PASSED, result.status());
        assertTrue(result.succeeded());
        assertEquals(List.of("workbook.xlsx"), executed);
        assertNotNull(result.duration());
        assertTrue(result.validation().isPassed());
    }

    @Test
    void failsWhenValidationFindsProblems() {
        ExcelTaskEngine engine = new ExcelTaskEngine(
                new TaskGenerator(new Random(1)),
                (generated, workbook) -> { },
                (generated, workbook) -> ValidationResult.failed(
                        List.of(ValidationIssue.of("cell B2 is wrong")), 1));

        TaskResult result = engine.run(task, Path.of("workbook.xlsx"));

        assertEquals(TaskStatus.FAILED, result.status());
        assertFalse(result.succeeded());
        assertTrue(result.validation().summary().contains("cell B2 is wrong"));
    }

    @Test
    void doesNotValidateWhenExecutionFails() {
        List<Boolean> validated = new ArrayList<>();
        ExcelTaskEngine engine = new ExcelTaskEngine(
                new TaskGenerator(new Random(1)),
                (generated, workbook) -> {
                    throw new TaskExecutionException("spreadsheet not available");
                },
                (generated, workbook) -> {
                    validated.add(true);
                    return ValidationResult.passed(1);
                });

        TaskResult result = engine.run(task, Path.of("workbook.xlsx"));

        assertEquals(TaskStatus.FAILED, result.status());
        assertTrue(validated.isEmpty());
        assertTrue(result.error().contains("spreadsheet not available"));
    }

    @Test
    void reportsValidationCrashesAsFailures() {
        ExcelTaskEngine engine = new ExcelTaskEngine(
                new TaskGenerator(new Random(1)),
                (generated, workbook) -> { },
                (generated, workbook) -> {
                    throw new IllegalStateException("corrupt workbook");
                });

        TaskResult result = engine.run(task, Path.of("workbook.xlsx"));

        assertEquals(TaskStatus.FAILED, result.status());
        assertTrue(result.error().contains("corrupt workbook"));
    }

    @Test
    void generatesTasksWithoutTouchingTheDesktop() {
        ExcelTaskEngine engine = new ExcelTaskEngine(
                new TaskGenerator(new Random(1)),
                (generated, workbook) -> { },
                (generated, workbook) -> ValidationResult.passed(1));

        ExcelTask generated = engine.nextTask(TaskCategory.SORTING, TaskDifficulty.ADVANCED);

        assertEquals(TaskCategory.SORTING, generated.category());
        assertEquals(TaskDifficulty.ADVANCED, generated.difficulty());
    }

    @Test
    void measuresDurationFromTheInjectedClock() {
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        Instant end = start.plusSeconds(90);
        Instant[] now = {start};
        ExcelTaskEngine engine = new ExcelTaskEngine(
                new TaskGenerator(new Random(1)),
                (generated, workbook) -> now[0] = end,
                (generated, workbook) -> ValidationResult.passed(1),
                () -> now[0]);

        TaskResult result = engine.run(task, Path.of("workbook.xlsx"));

        assertEquals(90, result.duration().getSeconds());
    }
}
