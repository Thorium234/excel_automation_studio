package com.excelstudio.session;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionConfigTest {

    @Test
    void exposesTheDocumentedPresets() {
        assertEquals(Duration.ofMinutes(30), SessionConfig.Preset.THIRTY_MINUTES.config().duration());
        assertEquals(Duration.ofHours(1), SessionConfig.Preset.ONE_HOUR.config().duration());
        assertEquals(Duration.ofHours(3), SessionConfig.Preset.THREE_HOURS.config().duration());
        assertEquals(Duration.ofHours(5), SessionConfig.Preset.FIVE_HOURS.config().duration());
        assertEquals(Duration.ofHours(8), SessionConfig.Preset.EIGHT_HOURS.config().duration());
        assertEquals(Duration.ofHours(10), SessionConfig.Preset.TEN_HOURS.config().duration());
    }

    @Test
    void supportsCustomHoursAndMinutes() {
        SessionConfig config = SessionConfig.custom(2, 45);

        assertEquals(Duration.ofMinutes(165), config.duration());
        assertEquals("2 h 45 min", config.display());
    }

    @Test
    void displaysWholeHoursWithoutMinutes() {
        assertEquals("3 h", SessionConfig.ofHours(3).display());
    }

    @Test
    void rejectsInvalidDurations() {
        assertThrows(IllegalArgumentException.class, () -> SessionConfig.ofMinutes(0));
        assertThrows(IllegalArgumentException.class, () -> SessionConfig.ofMinutes(-5));
        assertThrows(IllegalArgumentException.class, () -> SessionConfig.ofHours(25));
        assertThrows(IllegalArgumentException.class, () -> SessionConfig.custom(1, 60));
        assertThrows(IllegalArgumentException.class, () -> SessionConfig.custom(-1, 10));
    }

    @Test
    void presetLabelsAreHumanReadable() {
        assertEquals("30 minutes", SessionConfig.Preset.THIRTY_MINUTES.label());
        assertEquals("10 hours", SessionConfig.Preset.TEN_HOURS.toString());
    }
}
