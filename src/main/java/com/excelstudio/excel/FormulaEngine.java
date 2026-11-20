package com.excelstudio.excel;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.excelstudio.model.CellAddress;

/**
 * Catalogue of formulas used by generated exercises and helpers that build
 * formula text from dataset coordinates.
 *
 * <p>Expected values are always computed independently in Java; a formula is never
 * treated as correct merely because it was typed.
 */
public final class FormulaEngine {
    public record FormulaDefinition(String name, String signature, String description, String example) {
    }

    private static final List<FormulaDefinition> CATALOG = List.of(
            new FormulaDefinition("SUM", "SUM(number1, [number2], ...)", "Adds numbers", "=SUM(E2:E9)"),
            new FormulaDefinition("AVERAGE", "AVERAGE(number1, [number2], ...)", "Arithmetic mean", "=AVERAGE(F2:F9)"),
            new FormulaDefinition("MIN", "MIN(number1, [number2], ...)", "Smallest value", "=MIN(D2:D9)"),
            new FormulaDefinition("MAX", "MAX(number1, [number2], ...)", "Largest value", "=MAX(D2:D9)"),
            new FormulaDefinition("ROUND", "ROUND(number, num_digits)", "Rounds a number", "=ROUND(E2*2, 2)"),
            new FormulaDefinition("IF", "IF(test, value_if_true, value_if_false)", "Chooses between values", "=IF(D2>=60,\"Pass\",\"Fail\")"),
            new FormulaDefinition("AND", "AND(condition1, [condition2], ...)", "True when all conditions hold", "=AND(D2>=60,E2>0)"),
            new FormulaDefinition("OR", "OR(condition1, [condition2], ...)", "True when any condition holds", "=OR(C2=\"North\",C2=\"East\")"),
            new FormulaDefinition("COUNT", "COUNT(value1, [value2], ...)", "Counts numbers", "=COUNT(D2:D9)"),
            new FormulaDefinition("COUNTA", "COUNTA(value1, [value2], ...)", "Counts non-empty values", "=COUNTA(A2:A9)"),
            new FormulaDefinition("COUNTIF", "COUNTIF(range, criteria)", "Counts cells matching a criterion", "=COUNTIF(C2:C9,\"North\")"),
            new FormulaDefinition("COUNTIFS", "COUNTIFS(criteria_range1, criteria1, ...)", "Counts cells matching several criteria", "=COUNTIFS(C2:C9,\"North\",D2:D9,\">10\")"),
            new FormulaDefinition("SUMIF", "SUMIF(range, criteria, [sum_range])", "Adds cells matching a criterion", "=SUMIF(C2:C9,\"North\",E2:E9)"),
            new FormulaDefinition("VLOOKUP", "VLOOKUP(value, table, col_index, [range_lookup])", "Looks up a value in the first column", "=VLOOKUP(\"PRD-7001\",A2:F9,4,FALSE)"),
            new FormulaDefinition("XLOOKUP", "XLOOKUP(value, lookup_array, return_array)", "Looks up a value where supported", "=XLOOKUP(\"Aisha\",B2:B9,A2:A9)"),
            new FormulaDefinition("LEFT", "LEFT(text, [num_chars])", "Takes characters from the start", "=LEFT(A2,3)"),
            new FormulaDefinition("RIGHT", "RIGHT(text, [num_chars])", "Takes characters from the end", "=RIGHT(A2,3)"),
            new FormulaDefinition("MID", "MID(text, start, num_chars)", "Takes characters from the middle", "=MID(A2,5,2)"),
            new FormulaDefinition("CONCAT", "CONCAT(text1, [text2], ...)", "Joins text", "=CONCAT(B2,\" \",C2)"),
            new FormulaDefinition("TRIM", "TRIM(text)", "Removes extra spaces", "=TRIM(B2)"),
            new FormulaDefinition("LEN", "LEN(text)", "Counts characters", "=LEN(B2)"),
            new FormulaDefinition("TODAY", "TODAY()", "Current date", "=TODAY()"),
            new FormulaDefinition("DATE", "DATE(year, month, day)", "Builds a date", "=DATE(2024,5,1)"),
            new FormulaDefinition("YEAR", "YEAR(date)", "Extracts the year", "=YEAR(B2)"),
            new FormulaDefinition("MONTH", "MONTH(date)", "Extracts the month", "=MONTH(B2)"),
            new FormulaDefinition("DAY", "DAY(date)", "Extracts the day", "=DAY(B2)")
    );

    private FormulaEngine() {
    }

    public static List<FormulaDefinition> catalog() {
        return CATALOG;
    }

    public static Optional<FormulaDefinition> find(String functionName) {
        if (functionName == null) {
            return Optional.empty();
        }
        return CATALOG.stream()
                .filter(definition -> definition.name().equalsIgnoreCase(functionName.trim()))
                .findFirst();
    }

    public static boolean isSupported(String functionName) {
        return find(functionName).isPresent();
    }

    public static String rangeReference(int firstRow, int column, int lastRow) {
        return CellAddress.columnName(column) + (firstRow + 1)
                + ":" + CellAddress.columnName(column) + (lastRow + 1);
    }

    public static String sum(int firstRow, int column, int lastRow) {
        return "=SUM(" + rangeReference(firstRow, column, lastRow) + ")";
    }

    public static String average(int firstRow, int column, int lastRow) {
        return "=AVERAGE(" + rangeReference(firstRow, column, lastRow) + ")";
    }

    public static String countIf(int firstRow, int column, int lastRow, String criteria) {
        return "=COUNTIF(" + rangeReference(firstRow, column, lastRow) + ",\""
                + criteria.replace("\"", "\"\"") + "\")";
    }

    public static String sumIf(int firstRow, int criteriaColumn, int sumColumn, int lastRow, String criteria) {
        return "=SUMIF(" + rangeReference(firstRow, criteriaColumn, lastRow)
                + ",\"" + criteria.replace("\"", "\"\"") + "\","
                + rangeReference(firstRow, sumColumn, lastRow) + ")";
    }

    /**
     * Normalizes formula text for comparison: case, whitespace, absolute markers and
     * quotes around simple worksheet names are ignored.
     */
    public static String normalize(String formula) {
        if (formula == null) {
            return "";
        }
        String normalized = formula.trim().toUpperCase(Locale.ROOT)
                .replace("$", "")
                .replaceAll("\\s+", "");
        normalized = normalized.replaceAll("'([A-Za-z0-9_]+)'!", "$1!");
        return normalized;
    }
}
