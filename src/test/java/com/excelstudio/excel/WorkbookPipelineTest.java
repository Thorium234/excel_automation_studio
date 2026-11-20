package com.excelstudio.excel;

import java.nio.file.Path;
import java.util.Random;

import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end check: every generated exercise is carried out on a real workbook and
 * the saved file is then validated independently.
 */
class WorkbookPipelineTest {

    @TempDir
    Path outputDirectory;

    @Test
    void everyExerciseCanBeExecutedAndValidated() throws Exception {
        TaskGenerator generator = new TaskGenerator(new Random(99));
        WorkbookTaskExecutor executor = new WorkbookTaskExecutor();
        PoiTaskValidator validator = new PoiTaskValidator();

        for (TaskCategory category : TaskCategory.values()) {
            for (TaskDifficulty difficulty : TaskDifficulty.values()) {
                ExcelTask task = generator.generate(category, difficulty);
                Path workbook = outputDirectory.resolve(category.name() + "-" + difficulty + ".xlsx");

                executor.execute(task, workbook);
                ValidationResult result = validator.validate(task, workbook);

                assertTrue(result.isPassed(),
                        category + " / " + difficulty + " should pass but reported: " + result.summary());
            }
        }
    }
}
