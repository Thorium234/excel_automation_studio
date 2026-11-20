package com.excelstudio.excel;

import java.nio.file.Path;
import java.util.Random;

import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.ValidationResult;
import com.excelstudio.service.WorkbookService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoiTaskValidatorTest {

    @TempDir
    Path outputDirectory;

    private final TaskGenerator generator = new TaskGenerator(new Random(5));
    private final WorkbookTaskExecutor executor = new WorkbookTaskExecutor();
    private final PoiTaskValidator validator = new PoiTaskValidator();
    private final WorkbookService workbooks = new WorkbookService();

    @Test
    void failsWhenTheWorkbookWasNeverSaved() {
        ExcelTask task = generator.generate(TaskCategory.DATA_ENTRY, TaskDifficulty.BASIC);

        ValidationResult result = validator.validate(task, outputDirectory.resolve("missing.xlsx"));

        assertFalse(result.isPassed());
        assertTrue(result.summary().contains("not saved"));
    }

    @Test
    void detectsAnIncorrectCellValue() throws Exception {
        ExcelTask task = generator.generate(TaskCategory.DATA_ENTRY, TaskDifficulty.BASIC);
        Path workbook = outputDirectory.resolve("wrong-value.xlsx");
        executor.execute(task, workbook);

        try (Workbook open = workbooks.open(workbook)) {
            Sheet sheet = open.getSheet(task.sheetName());
            Cell cell = sheet.getRow(0).getCell(0);
            cell.setCellValue("Wrong header");
            workbooks.save(open, workbook);
        }

        ValidationResult result = validator.validate(task, workbook);

        assertFalse(result.isPassed());
        assertTrue(result.summary().contains("Wrong header"));
    }

    @Test
    void detectsAMissingWorksheet() throws Exception {
        ExcelTask task = generator.generate(TaskCategory.WORKSHEETS, TaskDifficulty.BASIC);
        Path workbook = outputDirectory.resolve("missing-sheet.xlsx");
        executor.execute(task, workbook);

        try (Workbook open = workbooks.open(workbook)) {
            open.removeSheetAt(open.getSheetIndex("Summary"));
            workbooks.save(open, workbook);
        }

        ValidationResult result = validator.validate(task, workbook);

        assertFalse(result.isPassed());
        assertTrue(result.summary().contains("Summary"));
    }

    @Test
    void detectsAWrongFormula() throws Exception {
        ExcelTask task = generator.generate(TaskCategory.FORMULAS, TaskDifficulty.BASIC);
        Path workbook = outputDirectory.resolve("wrong-formula.xlsx");
        executor.execute(task, workbook);

        var formulaRule = task.rules().stream()
                .filter(rule -> rule.type() == com.excelstudio.model.ValidationType.CELL_FORMULA)
                .findFirst()
                .orElseThrow();

        try (Workbook open = workbooks.open(workbook)) {
            Sheet sheet = open.getSheet(task.sheetName());
            sheet.getRow(formulaRule.row()).getCell(formulaRule.column())
                    .setCellFormula("SUM(A1:A2)");
            workbooks.save(open, workbook);
        }

        ValidationResult result = validator.validate(task, workbook);

        assertFalse(result.isPassed());
        assertTrue(result.summary().contains("SUM(A1:A2)"));
    }

    @Test
    void acceptsAWorkbookProducedByTheExecutor() throws Exception {
        ExcelTask task = generator.generate(TaskCategory.FORMULAS, TaskDifficulty.INTERMEDIATE);
        Path workbook = outputDirectory.resolve("valid.xlsx");

        executor.execute(task, workbook);

        assertTrue(validator.validate(task, workbook).isPassed());
    }
}
