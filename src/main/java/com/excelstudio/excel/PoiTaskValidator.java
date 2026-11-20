package com.excelstudio.excel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.excelstudio.model.ValidationResult;
import com.excelstudio.model.ValidationResult.ValidationIssue;
import com.excelstudio.model.ValidationRule;
import com.excelstudio.service.WorkbookService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a saved workbook with Apache POI.
 *
 * <p>Formulas are evaluated with POI's evaluator. The README explicitly warns that
 * POI is not Excel's calculation engine, so a formula's cached value is preferred
 * when the spreadsheet application has already calculated it.
 */
public final class PoiTaskValidator implements TaskValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoiTaskValidator.class);
    private static final double NUMERIC_TOLERANCE = 0.01;
    private static final double WIDTH_TOLERANCE = 2.0;
    private static final double HEIGHT_TOLERANCE = 3.0;

    private final WorkbookService workbooks;

    public PoiTaskValidator() {
        this(new WorkbookService());
    }

    public PoiTaskValidator(WorkbookService workbooks) {
        this.workbooks = workbooks;
    }

    @Override
    public ValidationResult validate(ExcelTask task, Path workbookPath) {
        if (!Files.isRegularFile(workbookPath)) {
            return ValidationResult.failed(
                    List.of(ValidationIssue.of("Workbook was not saved: " + workbookPath)), 0);
        }

        List<ValidationIssue> issues = new ArrayList<>();
        int checks = 0;
        try (Workbook workbook = workbooks.open(workbookPath)) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            if (workbook.getNumberOfSheets() == 0) {
                issues.add(ValidationIssue.of("Workbook contains no worksheets"));
            }
            for (ValidationRule rule : task.rules()) {
                checks++;
                checkRule(workbook, evaluator, rule, issues);
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Unable to inspect workbook {}", workbookPath, e);
            return ValidationResult.failed(
                    List.of(ValidationIssue.of("Workbook could not be read: " + e.getMessage())),
                    checks);
        }

        if (issues.isEmpty()) {
            return ValidationResult.passed(checks);
        }
        return ValidationResult.failed(issues, checks);
    }

    private void checkRule(Workbook workbook, FormulaEvaluator evaluator,
                           ValidationRule rule, List<ValidationIssue> issues) {
        switch (rule.type()) {
            case WORKBOOK_READABLE -> {
                // Opening the workbook already proved that it is readable.
            }
            case SHEET_PRESENT -> {
                if (workbook.getSheet(rule.sheet()) == null) {
                    issues.add(issue(rule, "worksheet '" + rule.sheet() + "' is missing"));
                }
            }
            case COLUMN_WIDTH -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    double actual = sheet.getColumnWidth(rule.column()) / 256.0;
                    double expected = number(rule.expected());
                    if (Math.abs(actual - expected) > WIDTH_TOLERANCE) {
                        issues.add(issue(rule, "column width is " + round(actual)
                                + " characters, expected about " + round(expected)));
                    }
                }
            }
            case ROW_HEIGHT -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    Row row = sheet.getRow(rule.row());
                    double actual = row == null ? sheet.getDefaultRowHeightInPoints()
                            : row.getHeightInPoints();
                    double expected = number(rule.expected());
                    if (Math.abs(actual - expected) > HEIGHT_TOLERANCE) {
                        issues.add(issue(rule, "row height is " + round(actual)
                                + " points, expected about " + round(expected)));
                    }
                }
            }
            case CELL_VALUE -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    checkCellValue(sheet, evaluator, rule, issues);
                }
            }
            case CELL_EMPTY -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    Cell cell = workbooks.cell(sheet, rule.row(), rule.column());
                    if (!workbooks.isBlank(cell)) {
                        issues.add(issue(rule, "cell " + reference(rule) + " still contains '"
                                + valueText(cell, evaluator) + "'"));
                    }
                }
            }
            case CELL_FORMULA -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    Cell cell = workbooks.cell(sheet, rule.row(), rule.column());
                    if (cell == null || cell.getCellType() != CellType.FORMULA) {
                        issues.add(issue(rule, "cell " + reference(rule) + " does not contain a formula"));
                    } else {
                        String actual = "=" + cell.getCellFormula();
                        String expected = String.valueOf(rule.expected());
                        if (!FormulaEngine.normalize(actual).equals(FormulaEngine.normalize(expected))) {
                            issues.add(issue(rule, "formula in " + reference(rule) + " is '"
                                    + actual + "', expected '" + expected + "'"));
                        }
                    }
                }
            }
            case CELL_BOLD -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    Cell cell = workbooks.cell(sheet, rule.row(), rule.column());
                    boolean bold = cell != null
                            && workbook.getFontAt(cell.getCellStyle().getFontIndex()).getBold();
                    if (!bold) {
                        issues.add(issue(rule, "cell " + reference(rule) + " is not bold"));
                    }
                }
            }
            case COLUMN_VALUES -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    checkColumnValues(sheet, evaluator, rule, issues);
                }
            }
            case CHART_PRESENT -> {
                Sheet sheet = requireSheet(workbook, rule, issues);
                if (sheet != null) {
                    int expected = (int) number(rule.expected());
                    int actual = chartCount(sheet);
                    if (actual < expected) {
                        issues.add(issue(rule, "worksheet '" + rule.sheet() + "' contains "
                                + actual + " chart(s), expected " + expected));
                    }
                }
            }
        }
    }

    private void checkCellValue(Sheet sheet, FormulaEvaluator evaluator,
                                ValidationRule rule, List<ValidationIssue> issues) {
        Cell cell = workbooks.cell(sheet, rule.row(), rule.column());
        Object expected = rule.expected();
        String location = reference(rule);

        if (expected instanceof Number number) {
            Double actual = numericValue(cell, evaluator);
            if (actual == null || Math.abs(actual - number.doubleValue()) > NUMERIC_TOLERANCE) {
                issues.add(issue(rule, "cell " + location + " contains "
                        + (actual == null ? "no number" : round(actual))
                        + ", expected " + round(number.doubleValue())));
            }
            return;
        }
        if (expected instanceof LocalDate date) {
            if (cell == null || cell.getCellType() != CellType.NUMERIC
                    || !DateUtil.isCellDateFormatted(cell)) {
                issues.add(issue(rule, "cell " + location + " does not contain a date"));
            } else if (!cell.getLocalDateTimeCellValue().toLocalDate().equals(date)) {
                issues.add(issue(rule, "cell " + location + " contains "
                        + cell.getLocalDateTimeCellValue().toLocalDate() + ", expected " + date));
            }
            return;
        }

        String actual = valueText(cell, evaluator);
        String wanted = String.valueOf(expected);
        if (!actual.trim().equals(wanted.trim())) {
            issues.add(issue(rule, "cell " + location + " contains '" + actual
                    + "', expected '" + wanted + "'"));
        }
    }

    private void checkColumnValues(Sheet sheet, FormulaEvaluator evaluator,
                                   ValidationRule rule, List<ValidationIssue> issues) {
        List<?> expected = (List<?>) rule.expected();
        for (int offset = 0; offset < expected.size(); offset++) {
            int rowIndex = rule.row() + offset;
            Cell cell = workbooks.cell(sheet, rowIndex, rule.column());
            String actual = valueText(cell, evaluator);
            String wanted = String.valueOf(expected.get(offset));
            if (!actual.trim().equals(wanted.trim())) {
                String location = CellReference.convertNumToColString(rule.column()) + (rowIndex + 1);
                issues.add(issue(rule, "cell " + location + " contains '" + actual
                        + "', expected '" + wanted + "' in the sorted order"));
                return;
            }
        }
    }

    private Sheet requireSheet(Workbook workbook, ValidationRule rule, List<ValidationIssue> issues) {
        Sheet sheet = workbook.getSheet(rule.sheet());
        if (sheet == null) {
            issues.add(issue(rule, "worksheet '" + rule.sheet() + "' is missing"));
        }
        return sheet;
    }

    private static Double numericValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                var value = evaluator.evaluate(cell);
                return value == null ? null : value.getNumberValue();
            } catch (RuntimeException e) {
                return null;
            }
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String valueText(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        try {
            return new DataFormatter().formatCellValue(cell, evaluator).trim();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static int chartCount(Sheet sheet) {
        if (sheet instanceof XSSFSheet xssfSheet) {
            XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
            return drawing == null ? 0 : drawing.getCharts().size();
        }
        return 0;
    }

    private static String reference(ValidationRule rule) {
        return CellReference.convertNumToColString(rule.column()) + (rule.row() + 1);
    }

    private static double number(Object value) {
        return ((Number) value).doubleValue();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static ValidationIssue issue(ValidationRule rule, String detail) {
        return ValidationIssue.of(rule.description() + ": " + detail);
    }
}
