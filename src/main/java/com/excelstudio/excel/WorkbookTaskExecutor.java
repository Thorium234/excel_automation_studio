package com.excelstudio.excel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.excelstudio.model.CellAddress;
import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.ValidationRule;
import com.excelstudio.service.WorkbookService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries out a task by editing a dedicated practice workbook and saving it.
 *
 * <p>This executor exists so the engine is useful without a spreadsheet installation
 * and so results are reproducible. It is not a simulated success: the workbook is
 * really modified and the validator re-opens the saved file.
 */
public final class WorkbookTaskExecutor implements TaskExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkbookTaskExecutor.class);

    private final WorkbookService workbooks;

    public WorkbookTaskExecutor() {
        this(new WorkbookService());
    }

    public WorkbookTaskExecutor(WorkbookService workbooks) {
        this.workbooks = workbooks;
    }

    @Override
    public void execute(ExcelTask task, Path workbookPath) throws TaskExecutionException {
        try {
            Path parent = workbookPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Workbook workbook;
            if (Files.isRegularFile(workbookPath)) {
                workbook = workbooks.open(workbookPath);
            } else {
                workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet(task.sheetName());
                if (task.dataset().isPresent()) {
                    workbooks.writeDataset(workbook, sheet, task.dataset().orElseThrow());
                }
            }

            try (Workbook managed = workbook) {
                apply(task, managed);
                workbooks.save(managed, workbookPath);
            }
            LOGGER.info("Executed task {} on {}", task.name(), workbookPath);
        } catch (IOException e) {
            throw new TaskExecutionException("Unable to prepare " + workbookPath, e);
        } catch (RuntimeException e) {
            throw new TaskExecutionException("Workbook operation failed: " + e.getMessage(), e);
        }
    }

    private void apply(ExcelTask task, Workbook workbook) {
        Sheet practice = workbook.getSheet(task.sheetName());
        if (practice == null) {
            practice = workbook.createSheet(task.sheetName());
        }
        if (task.category() == TaskCategory.DATA_ENTRY && task.dataset().isPresent()) {
            workbook.removeSheetAt(workbook.getSheetIndex(practice));
            practice = workbook.createSheet(task.sheetName());
            workbooks.writeDataset(workbook, practice, task.dataset().orElseThrow());
        }

        CellStyle dateStyle = workbooks.dateStyle(workbook);
        Map<String, CellStyle> boldStyles = new HashMap<>();
        Map<String, Boolean> formulaCells = new HashMap<>();

        for (ValidationRule rule : task.rules()) {
            if (rule.type() == com.excelstudio.model.ValidationType.CELL_FORMULA) {
                formulaCells.put(key(rule.sheet(), rule.row(), rule.column()), Boolean.TRUE);
            }
        }

        for (ValidationRule rule : task.rules()) {
            if (rule.sheet() == null) {
                continue;
            }
            Sheet sheet = workbook.getSheet(rule.sheet());
            switch (rule.type()) {
                case CELL_VALUE -> {
                    if (formulaCells.containsKey(key(rule.sheet(), rule.row(), rule.column()))) {
                        continue;
                    }
                    if (sheet == null) {
                        sheet = workbook.createSheet(rule.sheet());
                    }
                    Cell cell = cell(sheet, rule.row(), rule.column());
                    workbooks.setValue(cell, rule.expected(), dateStyle);
                }
                case CELL_EMPTY -> {
                    if (sheet != null) {
                        Cell cell = workbooks.cell(sheet, rule.row(), rule.column());
                        if (cell != null) {
                            cell.setBlank();
                        }
                    }
                }
                case CELL_FORMULA -> {
                    if (sheet == null) {
                        sheet = workbook.createSheet(rule.sheet());
                    }
                    String formula = String.valueOf(rule.expected());
                    cell(sheet, rule.row(), rule.column())
                            .setCellFormula(formula.startsWith("=") ? formula.substring(1) : formula);
                }
                case CELL_BOLD -> {
                    if (sheet != null) {
                        Cell cell = workbooks.cell(sheet, rule.row(), rule.column());
                        if (cell != null) {
                            CellStyle style = boldStyles.computeIfAbsent(rule.sheet(),
                                    name -> boldStyle(workbook, cell.getCellStyle()));
                            cell.setCellStyle(style);
                        }
                    }
                }
                case COLUMN_WIDTH -> {
                    if (sheet != null) {
                        sheet.setColumnWidth(rule.column(),
                                (int) Math.round(((Number) rule.expected()).doubleValue() * 256));
                    }
                }
                case ROW_HEIGHT -> {
                    if (sheet != null) {
                        Row row = sheet.getRow(rule.row());
                        if (row == null) {
                            row = sheet.createRow(rule.row());
                        }
                        row.setHeightInPoints((float) ((Number) rule.expected()).doubleValue());
                    }
                }
                case SHEET_PRESENT -> {
                    if (workbook.getSheet(rule.sheet()) == null) {
                        workbook.createSheet(rule.sheet());
                    }
                }
                case COLUMN_VALUES -> sortByRule(practice, rule, dateStyle);
                case CHART_PRESENT -> {
                    if (practice instanceof XSSFSheet xssfSheet) {
                        createChart((XSSFWorkbook) workbook, xssfSheet);
                    }
                }
                case WORKBOOK_READABLE -> {
                    // Saving the workbook is enough.
                }
            }
        }

        if (task.category() == TaskCategory.FILTERING) {
            int lastRow = practice.getLastRowNum();
            int lastColumn = task.dataset().map(Dataset::columnCount).orElse(1) - 1;
            practice.setAutoFilter(new CellRangeAddress(0, lastRow, 0, lastColumn));
        }
    }

    private void sortByRule(Sheet sheet, ValidationRule rule, CellStyle dateStyle) {
        int firstRow = rule.row();
        int lastRow = sheet.getLastRowNum();
        int lastColumn = 0;
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                lastColumn = Math.max(lastColumn, row.getLastCellNum());
            }
        }
        if (lastRow <= firstRow) {
            return;
        }

        List<Object[]> rows = new ArrayList<>();
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Object[] values = new Object[lastColumn];
            for (int column = 0; column < lastColumn; column++) {
                values[column] = readValue(workbooks.cell(sheet, rowIndex, column));
            }
            rows.add(values);
        }
        rows.sort(Comparator.comparing((Object[] row) -> row[rule.column()],
                WorkbookTaskExecutor::compareValues));

        for (int offset = 0; offset < rows.size(); offset++) {
            Row row = sheet.getRow(firstRow + offset);
            if (row == null) {
                row = sheet.createRow(firstRow + offset);
            }
            Object[] values = rows.get(offset);
            for (int column = 0; column < values.length; column++) {
                Cell cell = row.getCell(column);
                if (cell == null) {
                    cell = row.createCell(column);
                }
                workbooks.setValue(cell, values[column], dateStyle);
            }
        }
    }

    private static Object readValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate()
                    : cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> cell.getStringCellValue();
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static int compareValues(Object left, Object right) {
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    }

    private void createChart(XSSFWorkbook workbook, XSSFSheet sheet) {
        int lastRow = Math.max(1, sheet.getLastRowNum());
        int valueColumn = 4;
        if (sheet.getRow(1) != null && sheet.getRow(1).getLastCellNum() <= valueColumn) {
            valueColumn = Math.max(0, sheet.getRow(1).getLastCellNum() - 1);
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        anchor.setCol1(6);
        anchor.setCol2(14);
        anchor.setRow1(1);
        anchor.setRow2(16);

        XSSFChart chart = drawing.createChart(anchor);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

        XDDFDataSource<String> labels = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(1, lastRow, 0, 0));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(1, lastRow, valueColumn, valueColumn));

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setBarDirection(BarDirection.COL);
        data.addSeries(labels, values);
        chart.plot(data);
        chart.setTitleText("Practice chart");
        chart.setTitleOverlay(false);
    }

    private static CellStyle boldStyle(Workbook workbook, CellStyle source) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(source);

        org.apache.poi.ss.usermodel.Font original = workbook.getFontAt(source.getFontIndex());
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setFontName(original.getFontName());
        font.setFontHeightInPoints(original.getFontHeightInPoints());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static Cell cell(Sheet sheet, int row, int column) {
        Row sheetRow = sheet.getRow(row);
        if (sheetRow == null) {
            sheetRow = sheet.createRow(row);
        }
        Cell cell = sheetRow.getCell(column);
        return cell == null ? sheetRow.createCell(column) : cell;
    }

    private static String key(String sheet, int row, int column) {
        return CellAddress.of(sheet, row, column).qualifiedA1();
    }
}
