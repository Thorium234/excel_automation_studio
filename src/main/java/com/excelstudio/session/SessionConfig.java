package com.excelstudio.session;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable session duration. Presets match the documented 30 minute to 10 hour
 * options and support a custom hours/minutes value.
 */
public final class SessionConfig {
    public static final Duration MINIMUM = Duration.ofMinutes(1);
    public static final Duration MAXIMUM = Duration.ofHours(24);

    private final Duration duration;

    private SessionConfig(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Session duration must be positive");
        }
        if (duration.compareTo(MINIMUM) < 0) {
            throw new IllegalArgumentException("Session duration must be at least " + MINIMUM.toMinutes()
                    + " minute");
        }
        if (duration.compareTo(MAXIMUM) > 0) {
            throw new IllegalArgumentException("Session duration must not exceed " + MAXIMUM.toHours()
                    + " hours");
        }
        this.duration = duration;
    }

    public static SessionConfig ofMinutes(long minutes) {
        return new SessionConfig(Duration.ofMinutes(minutes));
    }

    public static SessionConfig ofHours(long hours) {
        return new SessionConfig(Duration.ofHours(hours));
    }

    public static SessionConfig custom(long hours, long minutes) {
        if (hours < 0 || minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("Custom duration needs non-negative hours and 0-59 minutes");
        }
        return new SessionConfig(Duration.ofHours(hours).plusMinutes(minutes));
    }

    public static SessionConfig preset(Preset preset) {
        Objects.requireNonNull(preset, "preset");
        return preset.config();
    }

    public Duration duration() {
        return duration;
    }

    public String display() {
        if (duration.toMinutes() % 60 == 0) {
            return duration.toHours() + " h";
        }
        return duration.toHours() + " h " + (duration.toMinutes() % 60) + " min";
    }

    @Override
    public String toString() {
        return display();
    }

    /**
     * Documented duration presets.
     */
    public enum Preset {
        THIRTY_MINUTES("30 minutes", ofMinutes(30)),
        ONE_HOUR("1 hour", ofHours(1)),
        THREE_HOURS("3 hours", ofHours(3)),
        FIVE_HOURS("5 hours", ofHours(5)),
        EIGHT_HOURS("8 hours", ofHours(8)),
        TEN_HOURS("10 hours", ofHours(10));

        private final String label;
        private final SessionConfig config;

        Preset(String label, SessionConfig config) {
            this.label = label;
            this.config = config;
        }

        public String label() {
            return label;
        }

        public SessionConfig config() {
            return config;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
