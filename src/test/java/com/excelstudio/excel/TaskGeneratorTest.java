package com.excelstudio.excel;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.ValidationRule;
import com.excelstudio.model.ValidationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskGeneratorTest {

    private final TaskGenerator generator = new TaskGenerator(new Random(2024));

    @Test
    void generatesAValidTaskForEveryCategoryAndDifficulty() {
        for (TaskCategory category : TaskCategory.values()) {
            for (TaskDifficulty difficulty : TaskDifficulty.values()) {
                ExcelTask task = generator.generate(category, difficulty);

                assertNotNull(task.id());
                assertFalse(task.name().isBlank(), category.name());
                assertFalse(task.description().isBlank(), category.name());
                assertFalse(task.steps().isEmpty(), category.name());
                assertFalse(task.rules().isEmpty(), category.name());
                assertEquals(category, task.category());
                assertEquals(difficulty, task.difficulty());
                assertTrue(task.workbookFileName().endsWith(".xlsx"), task.workbookFileName());
                assertTrue(task.dataset().isPresent(), category.name());
                assertEquals(difficulty.datasetRows(), task.dataset().orElseThrow().rowCount(),
                        category.name());
            }
        }
    }

    @Test
    void validationRulesReferenceExistingWorksheets() {
        for (TaskCategory category : TaskCategory.values()) {
            ExcelTask task = generator.generate(category, TaskDifficulty.BASIC);
            for (ValidationRule rule : task.rules()) {
                if (rule.type() == ValidationType.WORKBOOK_READABLE) {
                    continue;
                }
                assertNotNull(rule.sheet(), category.name());
                assertFalse(rule.sheet().isBlank(), category.name());
            }
        }
    }

    @Test
    void dataEntryExpectsRealValuesFromTheDataset() {
        ExcelTask task = generator.generate(TaskCategory.DATA_ENTRY, TaskDifficulty.BASIC);
        Dataset dataset = task.dataset().orElseThrow();

        for (int column = 0; column < dataset.columnCount(); column++) {
            int expectedColumn = column;
            assertTrue(task.rules().stream().anyMatch(rule ->
                    rule.type() == ValidationType.CELL_VALUE
                            && rule.row() == 0
                            && rule.column() == expectedColumn
                            && dataset.columns().get(expectedColumn).equals(rule.expected())));
        }
        for (int row = 0; row < dataset.rowCount(); row++) {
            for (int column = 0; column < dataset.columnCount(); column++) {
                int validationRow = row + 1;
                int expectedColumn = column;
                int expectedRow = row;
                assertTrue(task.rules().stream().anyMatch(rule ->
                        rule.type() == ValidationType.CELL_VALUE
                                && rule.row() == validationRow
                                && rule.column() == expectedColumn
                                && dataset.value(expectedRow, expectedColumn).toString().equals(String.valueOf(rule.expected()))),
                        "Missing DATA_ENTRY rule at row " + validationRow + ", column " + expectedColumn);
            }
        }
    }

    @Test
    void formulaTasksTargetACellBelowTheData() {
        for (TaskDifficulty difficulty : TaskDifficulty.values()) {
            ExcelTask task = generator.generate(TaskCategory.FORMULAS, difficulty);
            Dataset dataset = task.dataset().orElseThrow();
            ValidationRule formula = task.rules().stream()
                    .filter(rule -> rule.type() == ValidationType.CELL_FORMULA)
                    .findFirst()
                    .orElseThrow();

            assertTrue(formula.row() > dataset.rowCount(), difficulty.name());
            assertTrue(String.valueOf(formula.expected()).startsWith("="), difficulty.name());
        }
    }

    @Test
    void sortingTaskExpectsAFiveRowPrefix() {
        ExcelTask task = generator.generate(TaskCategory.SORTING, TaskDifficulty.BASIC);
        ValidationRule rule = task.rules().stream()
                .filter(candidate -> candidate.type() == ValidationType.COLUMN_VALUES)
                .findFirst()
                .orElseThrow();

        assertEquals(5, ((java.util.List<?>) rule.expected()).size());
    }

    @Test
    void dataCleaningTaskStartsWithPaddedNamesAndExpectsTrimmedOnes() {
        ExcelTask task = generator.generate(TaskCategory.DATA_CLEANING, TaskDifficulty.BASIC);
        Dataset dataset = task.dataset().orElseThrow();

        assertTrue(String.valueOf(dataset.value(0, 1)).startsWith(" "));
        assertFalse(String.valueOf(task.rules().get(0).expected()).startsWith(" "));
    }

    @Test
    void idsAreUnique() {
        Set<String> ids = new HashSet<>();
        for (TaskCategory category : TaskCategory.values()) {
            assertTrue(ids.add(generator.generate(category, TaskDifficulty.INTERMEDIATE).id()));
        }
    }
}
