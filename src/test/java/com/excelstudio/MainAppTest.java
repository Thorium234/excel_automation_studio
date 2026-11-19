package com.excelstudio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainAppTest {
    @Test
    void applicationCanBeReferenced() {
        assertDoesNotThrow(() -> Class.forName("com.excelstudio.MainApp"));
    }
}
