package com.excelstudio.excel;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormulaEngineTest {

    @Test
    void catalogueContainsTheRequiredFunctions() {
        List<String> required = List.of("SUM", "AVERAGE", "MIN", "MAX", "ROUND", "IF", "AND", "OR",
                "COUNT", "COUNTA", "COUNTIF", "COUNTIFS", "VLOOKUP", "LEFT", "RIGHT", "MID",
                "CONCAT", "TRIM", "LEN", "TODAY", "DATE", "YEAR", "MONTH", "DAY");

        for (String function : required) {
            assertTrue(FormulaEngine.isSupported(function), function);
        }
    }

    @Test
    void everyDefinitionHasACompleteEntry() {
        for (FormulaEngine.FormulaDefinition definition : FormulaEngine.catalog()) {
            assertFalse(definition.name().isBlank());
            assertFalse(definition.signature().isBlank());
            assertFalse(definition.description().isBlank());
            assertTrue(definition.example().startsWith("="), definition.name());
        }
    }

    @Test
    void buildsRangeAndAggregateFormulas() {
        assertEquals("E2:E9", FormulaEngine.rangeReference(1, 4, 8));
        assertEquals("=SUM(E2:E9)", FormulaEngine.sum(1, 4, 8));
        assertEquals("=AVERAGE(F2:F9)", FormulaEngine.average(1, 5, 8));
        assertEquals("=COUNTIF(D2:D9,\"North\")", FormulaEngine.countIf(1, 3, 8, "North"));
        assertEquals("=SUMIF(C2:C9,\"East\",E2:E9)", FormulaEngine.sumIf(1, 2, 4, 8, "East"));
    }

    @Test
    void escapesQuotesInCriteria() {
        assertTrue(FormulaEngine.countIf(1, 0, 4, "a\"b").contains("\"a\"\"b\""));
    }

    @Test
    void normalizesFormulasForComparison() {
        assertEquals(FormulaEngine.normalize("=sum( e2 : e9 )"), FormulaEngine.normalize("=SUM($E$2:$E$9)"));
        assertEquals(FormulaEngine.normalize("=SUM('Practice'!E2:E9)"),
                FormulaEngine.normalize("=SUM(Practice!E2:E9)"));
    }

    @Test
    void isCaseInsensitiveWhenLookingUpFunctions() {
        assertTrue(FormulaEngine.find("sum").isPresent());
        assertFalse(FormulaEngine.find("not-a-function").isPresent());
        assertFalse(FormulaEngine.find(null).isPresent());
    }
}
