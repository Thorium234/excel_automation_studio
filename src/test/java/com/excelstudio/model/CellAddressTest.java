package com.excelstudio.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CellAddressTest {

    @Test
    void convertsColumnIndexesToLetters() {
        assertEquals("A", CellAddress.columnName(0));
        assertEquals("Z", CellAddress.columnName(25));
        assertEquals("AA", CellAddress.columnName(26));
        assertEquals("AZ", CellAddress.columnName(51));
        assertEquals("BA", CellAddress.columnName(52));
        assertEquals("ZZ", CellAddress.columnName(701));
        assertEquals("AAA", CellAddress.columnName(702));
    }

    @Test
    void formatsA1References() {
        assertEquals("B3", CellAddress.of("Practice", 2, 1).a1());
        assertEquals("'Practice'!B3", CellAddress.of("Practice", 2, 1).qualifiedA1());
    }

    @Test
    void rejectsInvalidAddresses() {
        assertThrows(IllegalArgumentException.class, () -> CellAddress.of(" ", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> CellAddress.of("S", -1, 0));
        assertThrows(IllegalArgumentException.class, () -> CellAddress.of("S", 0, CellAddress.MAX_COLUMNS));
        assertThrows(IllegalArgumentException.class, () -> CellAddress.columnName(-1));
    }

    @Test
    void buildsQuotedRangeReferences() {
        assertEquals("'Practice'!B2:D9", CellAddress.range("Practice", 1, 1, 8, 3));
    }
}
