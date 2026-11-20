package com.excelstudio.excel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import com.excelstudio.model.CellAddress;
import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.ValidationRule;

/**
 * Builds exercise definitions from datasets. Every expected value is calculated
 * from the generated dataset, so a task can never expect a value that the data
 * does not contain.
 */
public final class TaskGenerator {
    private static final String SHEET = "Practice";

    private final Random random;
    private final DatasetGenerator datasets;

    public TaskGenerator() {
        this(new Random());
    }

    public TaskGenerator(Random random) {
        this.random = random == null ? new Random() : random;
        this.datasets = new DatasetGenerator(this.random);
    }

    public ExcelTask generate(TaskCategory category, TaskDifficulty difficulty) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(difficulty, "difficulty");
        return switch (category) {
            case DATA_ENTRY -> dataEntry(difficulty);
            case CELL_NAVIGATION -> cellNavigation(difficulty);
            case EDITING -> editing(difficulty);
            case DELETION -> deletion(difficulty);
            case FORMULAS -> formulas(difficulty);
            case FORMATTING -> formatting(difficulty);
            case COLUMN_MANAGEMENT -> columnManagement(difficulty);
            case ROW_MANAGEMENT -> rowManagement(difficulty);
            case SORTING -> sorting(difficulty);
            case FILTERING -> filtering(difficulty);
            case DATA_CLEANING -> dataCleaning(difficulty);
            case CHARTS -> charts(difficulty);
            case WORKSHEETS -> worksheets(difficulty);
            case WORKBOOK_MANAGEMENT -> workbookManagement(difficulty);
        };
    }

    private ExcelTask dataEntry(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.EMPLOYEES, difficulty);
        ExcelTask.Builder task = base(TaskCategory.DATA_ENTRY, difficulty, data, "Enter employee records")
                .description("Type the employee table into the practice worksheet, starting at cell A1.")
                .step("Enter the header row in row 1.")
                .step("Enter all " + data.rowCount() + " employee rows below the header.");

        for (int column = 0; column < data.columnCount(); column++) {
            task.rule(cellValue(0, column, data.columns().get(column)));
        }
        for (int row = 0; row < data.rowCount(); row++) {
            for (int column = 0; column < data.columnCount(); column++) {
                task.rule(cellValue(row + 1, column, data.value(row, column)));
            }
        }
        return task.build();
    }

    private ExcelTask cellNavigation(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.CUSTOMERS, difficulty);
        int row = 4;
        int column = 7;
        Object value = data.value(0, 3);

        return base(TaskCategory.CELL_NAVIGATION, difficulty, data, "Navigate with the Name Box")
                .description("Select cell H5 without scrolling, then enter " + value + ".")
                .step("Click the Name Box and type H5, then press Enter.")
                .step("Type the requested value and confirm the entry.")
                .rule(cellValue(row, column, value))
                .build();
    }

    private ExcelTask editing(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.EMPLOYEES, difficulty);
        Object oldValue = data.value(0, 1);
        Object newValue = oldValue + " Jr.";

        return base(TaskCategory.EDITING, difficulty, data, "Edit a cell value")
                .description("Replace the first name in cell B2 (" + oldValue + ") with " + newValue + ".")
                .step("Select cell B2.")
                .step("Edit the value without disturbing the rest of the table.")
                .rule(cellValue(1, 1, newValue))
                .build();
    }

    private ExcelTask deletion(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.EXPENSES, difficulty);
        ExcelTask.Builder task = base(TaskCategory.DELETION, difficulty, data, "Clear a block of cells")
                .description("Clear the contents of cells C2:C5, leaving the other columns intact.")
                .step("Select the range C2:C5.")
                .step("Delete the cell contents.");
        for (int row = 1; row <= 4; row++) {
            task.rule(cellEmpty(row, 2));
        }
        return task.build();
    }

    private ExcelTask formulas(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.SALES, difficulty);
        int lastDataRow = data.rowCount();
        int totalRow = lastDataRow + 1;
        int column;
        String formula;
        Object expected;
        String target;

        if (difficulty == TaskDifficulty.ADVANCED) {
            column = 3;
            String criteria = String.valueOf(data.value(0, column));
            formula = FormulaEngine.countIf(1, column, lastDataRow, criteria);
            expected = (int) data.columnValues(data.columns().get(column)).stream()
                    .filter(criteria::equals)
                    .count();
            target = CellAddress.columnName(column) + (totalRow + 1);
        } else if (difficulty == TaskDifficulty.INTERMEDIATE) {
            column = 5;
            List<Double> values = data.numericValues(data.columns().get(column));
            double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            formula = FormulaEngine.average(1, column, lastDataRow);
            expected = Math.round(average * 100.0) / 100.0;
            target = CellAddress.columnName(column) + (totalRow + 1);
        } else {
            column = 4;
            List<Double> values = data.numericValues(data.columns().get(column));
            double sum = values.stream().mapToDouble(Double::doubleValue).sum();
            formula = FormulaEngine.sum(1, column, lastDataRow);
            expected = (int) sum;
            target = CellAddress.columnName(column) + (totalRow + 1);
        }

        return base(TaskCategory.FORMULAS, difficulty, data, "Summarise a column with a formula")
                .description("Enter " + formula + " in cell " + target + " below the table.")
                .step("Select cell " + target + ".")
                .step("Type the formula exactly as shown and press Enter.")
                .rule(ValidationRule.cellFormula(CellAddress.of(SHEET, totalRow, column), formula))
                .rule(cellValue(totalRow, column, expected))
                .build();
    }

    private ExcelTask formatting(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.PRODUCTS, difficulty);
        ExcelTask.Builder task = base(TaskCategory.FORMATTING, difficulty, data, "Format the header row")
                .description("Make the whole header row A1:F1 bold.")
                .step("Select the header row.")
                .step("Apply bold formatting.");
        for (int column = 0; column < data.columnCount(); column++) {
            task.rule(ValidationRule.cellBold(CellAddress.of(SHEET, 0, column)));
        }
        return task.build();
    }

    private ExcelTask columnManagement(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.INVENTORY, difficulty);
        return base(TaskCategory.COLUMN_MANAGEMENT, difficulty, data, "Resize a column")
                .description("Set the width of column B to about 22 characters so the product names fit.")
                .step("Select column B.")
                .step("Adjust its width to approximately 22 characters.")
                .rule(ValidationRule.columnWidth(SHEET, 1, 22))
                .build();
    }

    private ExcelTask rowManagement(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.ATTENDANCE, difficulty);
        return base(TaskCategory.ROW_MANAGEMENT, difficulty, data, "Adjust a row height")
                .description("Increase the height of row 1 to about 30 points.")
                .step("Select row 1.")
                .step("Set the row height to approximately 30 points.")
                .rule(ValidationRule.rowHeight(SHEET, 0, 30))
                .build();
    }

    private ExcelTask sorting(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.EMPLOYEES, difficulty);
        List<String> sorted = data.columnValues("Employee ID").stream()
                .map(String::valueOf)
                .sorted(Comparator.naturalOrder())
                .toList();
        List<String> expectedHead = sorted.subList(0, Math.min(5, sorted.size()));

        return base(TaskCategory.SORTING, difficulty, data, "Sort records by ID")
                .description("Sort the whole table by Employee ID in ascending order.")
                .step("Select the data range including the header.")
                .step("Sort ascending by Employee ID.")
                .rule(ValidationRule.columnValues(SHEET, 1, 0, expectedHead))
                .build();
    }

    private ExcelTask filtering(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.SALES, difficulty);
        int lastDataRow = data.rowCount();
        int regionColumn = data.columnIndex("Region");
        String criteria = "North";
        int count = (int) data.columnValues("Region").stream().filter(criteria::equals).count();

        return base(TaskCategory.FILTERING, difficulty, data, "Count filtered rows")
                .description("Filter the table to Region = North, then enter a COUNTIF formula in H2 that returns "
                        + count + ".")
                .step("Apply a filter to the Region column.")
                .step("Enter =COUNTIF(" + CellAddress.columnName(regionColumn) + "2:"
                        + CellAddress.columnName(regionColumn) + (lastDataRow + 1) + ",\"" + criteria + "\") in H2.")
                .rule(ValidationRule.cellFormula(CellAddress.of(SHEET, 1, 7),
                        FormulaEngine.countIf(1, regionColumn, lastDataRow, criteria)))
                .rule(cellValue(1, 7, count))
                .build();
    }

    private ExcelTask dataCleaning(TaskDifficulty difficulty) {
        Dataset clean = datasets.generate(DatasetType.CUSTOMERS, difficulty);
        List<List<Object>> dirtyRows = new ArrayList<>();
        for (List<Object> row : clean.rows()) {
            List<Object> copy = new ArrayList<>(row);
            copy.set(1, "  " + row.get(1) + "   ");
            dirtyRows.add(List.copyOf(copy));
        }
        Dataset dirty = clean.withRows(dirtyRows);

        ExcelTask.Builder task = base(TaskCategory.DATA_CLEANING, difficulty, dirty, "Clean padded names")
                .description("Remove the extra leading and trailing spaces from the names in B2:B6.")
                .step("Fix every padded name in column B, using TRIM or manual editing.");

        for (int row = 0; row < Math.min(5, clean.rowCount()); row++) {
            task.rule(cellValue(row + 1, 1, clean.value(row, 1)));
        }
        return task.build();
    }

    private ExcelTask charts(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.SALES, difficulty);
        int lastDataRow = data.rowCount();
        String range = "A1:" + CellAddress.columnName(data.columnCount() - 1) + (lastDataRow + 1);

        return base(TaskCategory.CHARTS, difficulty, data, "Create a column chart")
                .description("Create a column chart from the range " + range + ".")
                .step("Select the range " + range + ".")
                .step("Insert a column chart and place it beside the table.")
                .rule(ValidationRule.chartPresent(SHEET, 1))
                .build();
    }

    private ExcelTask worksheets(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.STUDENTS, difficulty);
        int total = data.numericValues("Score").stream().mapToInt(Double::intValue).sum();
        String summary = "Summary";

        return base(TaskCategory.WORKSHEETS, difficulty, data, "Create a summary worksheet")
                .description("Create a worksheet named " + summary
                        + " and enter the total of the Score column in its cell A1 (" + total + ").")
                .step("Insert a new worksheet.")
                .step("Rename it to " + summary + ".")
                .step("Enter =SUM(D2:D" + (data.rowCount() + 1) + ") in A1 of the new worksheet.")
                .rule(ValidationRule.sheetPresent(summary))
                .rule(ValidationRule.cellFormula(CellAddress.of(summary, 0, 0),
                        "=SUM(" + SHEET + "!D2:D" + (data.rowCount() + 1) + ")"))
                .rule(ValidationRule.cellValue(CellAddress.of(summary, 0, 0), total))
                .build();
    }

    private ExcelTask workbookManagement(TaskDifficulty difficulty) {
        Dataset data = datasets.generate(DatasetType.EXPENSES, difficulty);
        ExcelTask task = base(TaskCategory.WORKBOOK_MANAGEMENT, difficulty, data, "Save the practice workbook")
                .description("Save the workbook as " + "practice-workbook-management.xlsx"
                        + " in the configured output directory.")
                .step("Use Save As.")
                .step("Choose the output directory and keep the .xlsx format.")
                .rule(ValidationRule.workbookReadable())
                .rule(ValidationRule.sheetPresent(SHEET))
                .build();
        return task;
    }

    private ExcelTask.Builder base(TaskCategory category, TaskDifficulty difficulty,
                                   Dataset data, String name) {
        return ExcelTask.builder()
                .name(name)
                .category(category)
                .difficulty(difficulty)
                .sheetName(SHEET)
                .dataset(data)
                .workbookFileName("practice-" + category.name().toLowerCase(Locale.ROOT).replace('_', '-') + ".xlsx");
    }

    private static ValidationRule cellValue(int row, int column, Object expected) {
        return ValidationRule.cellValue(CellAddress.of(SHEET, row, column), expected);
    }

    private static ValidationRule cellEmpty(int row, int column) {
        return ValidationRule.cellEmpty(CellAddress.of(SHEET, row, column));
    }
}
