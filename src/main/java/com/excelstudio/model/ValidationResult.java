package com.excelstudio.model;

import java.time.Instant;
import java.util.List;

/**
 * Result of validating a workbook against a task's rules.
 */
public final class ValidationResult {
    private final List<ValidationIssue> issues;
    private final int checksPerformed;

    private ValidationResult(List<ValidationIssue> issues, int checksPerformed) {
        this.issues = List.copyOf(issues);
        this.checksPerformed = checksPerformed;
    }

    public static ValidationResult passed(int checksPerformed) {
        return new ValidationResult(List.of(), checksPerformed);
    }

    public static ValidationResult failed(List<ValidationIssue> issues, int checksPerformed) {
        if (issues.isEmpty()) {
            throw new IllegalArgumentException("A failed validation needs at least one issue");
        }
        return new ValidationResult(issues, checksPerformed);
    }

    public boolean isPassed() {
        return issues.isEmpty();
    }

    public List<ValidationIssue> issues() {
        return issues;
    }

    public int checksPerformed() {
        return checksPerformed;
    }

    public String summary() {
        if (isPassed()) {
            return checksPerformed + " check(s) passed";
        }
        StringBuilder text = new StringBuilder(issues.size() + " check(s) failed");
        for (ValidationIssue issue : issues) {
            text.append(System.lineSeparator()).append("- ").append(issue.message());
        }
        return text.toString();
    }

    public record ValidationIssue(String message, Instant observedAt) {
        public ValidationIssue {
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Issue message must not be blank");
            }
            observedAt = observedAt == null ? Instant.now() : observedAt;
        }

        public static ValidationIssue of(String message) {
            return new ValidationIssue(message, Instant.now());
        }
    }
}
