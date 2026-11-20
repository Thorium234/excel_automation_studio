package com.excelstudio.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import com.excelstudio.excel.Dataset;
import com.excelstudio.excel.ExcelTask;
import com.excelstudio.model.ValidationResult.ValidationIssue;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates, reads and saves practice workbooks. Apache POI handles file structure
 * only; it is not treated as a replacement for the spreadsheet application's
 * calculation engine.
 */
public final class WorkbookService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkbookService.class);
    private static final String DATE_FORMAT = "yyyy-mm-dd";
    private static final int DEFAULT_COLUMN_WIDTH = 18;
    private static final double DEFAULT_ROW_HEIGHT = 15.0;

    public Path createPracticeWorkbook(ExcelTask task, Path outputDirectory) throws IOException {
        Dataset dataset = task.dataset()
                .orElseThrow(() -> new IOException("Task has no dataset to write"));

        Files.createDirectories(outputDirectory);
        Path target = uniquePath(outputDirectory, task.workbookFileName());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(task.sheetName());
            writeDataset(workbook, sheet, dataset);
            save(workbook, target);
        }
        LOGGER.info("Created practice workbook {}", target);
        return target;
    }

    public void writeDataset(Workbook workbook, Sheet sheet, Dataset dataset) {
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle dateStyle = dateStyle(workbook);

        Row header = sheet.createRow(0);
        for (int column = 0; column < dataset.columnCount(); column++) {
            Cell cell = header.createCell(column);
            cell.setCellValue(dataset.columns().get(column));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(column, DEFAULT_COLUMN_WIDTH * 256);
        }
        header.setHeightInPoints((float) (DEFAULT_ROW_HEIGHT + 3));

        for (int rowIndex = 0; rowIndex < dataset.rowCount(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            for (int column = 0; column < dataset.columnCount(); column++) {
                setValue(row.createCell(column), dataset.value(rowIndex, column), dateStyle);
            }
            row.setHeightInPoints((float) DEFAULT_ROW_HEIGHT);
        }
    }

    public void setValue(Cell cell, Object value, CellStyle dateStyle) {
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean flag) {
            cell.setCellValue(flag);
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDateTime dateTime) {
            cell.setCellValue(dateTime);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    public CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    public CellStyle dateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(DATE_FORMAT));
        return style;
    }

    public Workbook open(Path workbookPath) throws IOException {
        if (!Files.isRegularFile(workbookPath)) {
            throw new IOException("Workbook does not exist: " + workbookPath);
        }
        try (InputStream in = Files.newInputStream(workbookPath)) {
            return WorkbookFactory.create(in);
        }
    }

    public void save(Workbook workbook, Path target) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temporary)) {
            workbook.write(out);
        }
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public Path uniquePath(Path directory, String fileName) {
        Path candidate = directory.resolve(fileName);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        String base = fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")
                ? fileName.substring(0, fileName.length() - 5)
                : fileName;
        int counter = 2;
        while (Files.exists(candidate)) {
            candidate = directory.resolve(base + "-" + counter + ".xlsx");
            counter++;
        }
        return candidate;
    }

    public ValidationResultCheck checkReadable(Path workbookPath) {
        if (!Files.isRegularFile(workbookPath)) {
            return ValidationResultCheck.failure("Workbook was not saved: " + workbookPath);
        }
        try (InputStream in = Files.newInputStream(workbookPath);
             Workbook workbook = WorkbookFactory.create(in)) {
            if (workbook.getNumberOfSheets() == 0) {
                return ValidationResultCheck.failure("Workbook contains no worksheets");
            }
            return ValidationResultCheck.success();
        } catch (IOException | RuntimeException e) {
            return ValidationResultCheck.failure("Workbook is not readable: " + e.getMessage());
        }
    }

    public Sheet requireSheet(Workbook workbook, String name, java.util.List<ValidationIssue> issues) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet == null) {
            issues.add(ValidationIssue.of("Worksheet '" + name + "' is missing"));
        }
        return sheet;
    }

    public Cell cell(Sheet sheet, int row, int column) {
        Row sheetRow = sheet.getRow(row);
        return sheetRow == null ? null : sheetRow.getCell(column);
    }

    public String text(Cell cell) {
        if (cell == null) {
            return "";
        }
        return new DataFormatter().formatCellValue(cell);
    }

    public boolean isBlank(Cell cell) {
        if (cell == null) {
            return true;
        }
        return cell.getCellType() == CellType.BLANK
                || (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isBlank());
    }

    public record ValidationResultCheck(boolean ok, String message) {
        static ValidationResultCheck success() {
            return new ValidationResultCheck(true, "");
        }

        static ValidationResultCheck failure(String message) {
            return new ValidationResultCheck(false, message);
        }
    }
}
