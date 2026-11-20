package com.excelstudio.excel;

import java.util.List;
import java.util.Random;

import com.excelstudio.model.TaskDifficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetGeneratorTest {

    @Test
    void generatesEveryDatasetTypeWithConsistentColumns() {
        DatasetGenerator generator = new DatasetGenerator(new Random(7));

        for (DatasetType type : DatasetType.values()) {
            Dataset dataset = generator.generate(type, 10);
            assertEquals(10, dataset.rowCount(), type.name());
            assertTrue(dataset.columnCount() >= 4, type.name());
            for (int row = 0; row < dataset.rowCount(); row++) {
                assertEquals(dataset.columnCount(), dataset.row(row).size(), type.name());
            }
        }
    }

    @Test
    void usesTheRequestedDifficultyRowCount() {
        DatasetGenerator generator = new DatasetGenerator(new Random(3));

        assertEquals(TaskDifficulty.BASIC.datasetRows(),
                generator.generate(DatasetType.SALES, TaskDifficulty.BASIC).rowCount());
        assertEquals(TaskDifficulty.ADVANCED.datasetRows(),
                generator.generate(DatasetType.SALES, TaskDifficulty.ADVANCED).rowCount());
    }

    @Test
    void isDeterministicForTheSameSeed() {
        Dataset first = new DatasetGenerator(new Random(11)).generate(DatasetType.EMPLOYEES, 6);
        Dataset second = new DatasetGenerator(new Random(11)).generate(DatasetType.EMPLOYEES, 6);

        assertEquals(first.columns(), second.columns());
        assertEquals(first.rows(), second.rows());
    }

    @Test
    void producesExpectedColumnLayouts() {
        DatasetGenerator generator = new DatasetGenerator(new Random(5));

        assertEquals(List.of("Employee ID", "First Name", "Last Name", "Department", "Salary", "Hire Date"),
                generator.generate(DatasetType.EMPLOYEES, 4).columns());
        assertEquals(List.of("Order ID", "Order Date", "Product", "Region", "Units", "Unit Price"),
                generator.generate(DatasetType.SALES, 4).columns());
    }

    @Test
    void generatesValuesThatCanBeWrittenToACell() {
        DatasetGenerator generator = new DatasetGenerator(new Random(9));
        for (DatasetType type : DatasetType.values()) {
            Dataset dataset = generator.generate(type, 5);
            dataset.rows().forEach(row -> row.forEach(value -> assertNotNull(
                    switch (value) {
                        case String ignored -> "text";
                        case Number ignored -> "number";
                        case Boolean ignored -> "boolean";
                        case java.time.LocalDate ignored -> "date";
                        default -> null;
                    })));
        }
    }

    @Test
    void validatesItsArguments() {
        DatasetGenerator generator = new DatasetGenerator(new Random(1));

        assertThrows(IllegalArgumentException.class, () -> generator.generate(null, 5));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(DatasetType.SALES, 0));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(DatasetType.SALES, 501));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(DatasetType.SALES, null));
    }

    @Test
    void numericColumnsAreReallyNumeric() {
        DatasetGenerator generator = new DatasetGenerator(new Random(13));
        for (DatasetType type : DatasetType.values()) {
            Dataset dataset = generator.generate(type, 6);
            int numericColumn = dataset.firstNumericColumn();
            assertFalse(dataset.numericValues(dataset.columns().get(numericColumn)).isEmpty());
        }
    }
}
