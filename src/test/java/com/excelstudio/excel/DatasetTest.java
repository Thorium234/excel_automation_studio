package com.excelstudio.excel;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetTest {

    private static Dataset dataset() {
        return new Dataset("Test", List.of("ID", "Name", "Amount"), List.of(
                Dataset.rowOf("A-1", "Alpha", 10),
                Dataset.rowOf("A-2", "Beta", 2.5),
                Dataset.rowOf("A-3", "Gamma", 7)));
    }

    @Test
    void exposesRowsAndColumns() {
        Dataset dataset = dataset();

        assertEquals(3, dataset.rowCount());
        assertEquals(3, dataset.columnCount());
        assertEquals("Beta", dataset.value(1, 1));
        assertEquals(List.of("A-1", "A-2", "A-3"), dataset.columnValues("ID"));
        assertEquals(List.of(10.0, 2.5, 7.0), dataset.numericValues("Amount"));
        assertEquals(2, dataset.firstNumericColumn());
    }

    @Test
    void isCaseInsensitiveWhenLookingUpColumns() {
        assertEquals(1, dataset().columnIndex("name"));
        assertTrue(dataset().hasColumn("NAME"));
    }

    @Test
    void rejectsDuplicateOrBlankColumns() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dataset("T", List.of("ID", "id"), List.of(Dataset.rowOf(1, 2))));
        assertThrows(IllegalArgumentException.class,
                () -> new Dataset("T", List.of("ID", " "), List.of(Dataset.rowOf(1, 2))));
    }

    @Test
    void rejectsRaggedRowsAndNulls() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dataset("T", List.of("A", "B"), List.of(Dataset.rowOf(1))));
        assertThrows(IllegalArgumentException.class,
                () -> new Dataset("T", List.of("A"), java.util.Arrays.asList(
                        java.util.Arrays.asList((Object) null))));
    }

    @Test
    void rejectsUnsupportedValueTypes() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dataset("T", List.of("A"), List.of(Dataset.rowOf(new Object()))));
    }

    @Test
    void copiesRowsImmutably() {
        Dataset dataset = dataset();
        assertThrows(UnsupportedOperationException.class, () -> dataset.rows().add(List.of(1, 2, 3)));
        assertThrows(UnsupportedOperationException.class, () -> dataset.columns().add("Extra"));
    }

    @Test
    void formatsDatesAndDoublesForDisplay() {
        Dataset dataset = new Dataset("T", List.of("When", "Value"),
                List.of(Dataset.rowOf(LocalDate.of(2024, 5, 1), 12.345)));
        assertEquals("2024-05-01", dataset.display(0, 0));
        assertEquals("12.35", dataset.display(0, 1));
    }
}
