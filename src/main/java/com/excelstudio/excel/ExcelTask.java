package com.excelstudio.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.excelstudio.model.CellAddress;
import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.ValidationRule;

/**
 * A generated exercise definition. Tasks are data only; they contain no JavaFX or
 * desktop automation code so the same task can be executed, replayed or validated.
 */
public final class ExcelTask {
    private final String id;
    private final String name;
    private final String description;
    private final TaskCategory category;
    private final TaskDifficulty difficulty;
    private final String sheetName;
    private final Dataset dataset;
    private final List<String> steps;
    private final List<ValidationRule> rules;
    private final String workbookFileName;

    private ExcelTask(Builder builder) {
        this.id = builder.id == null ? "TASK-" + UUID.randomUUID() : builder.id;
        this.name = requireText(builder.name, "name");
        this.description = requireText(builder.description, "description");
        this.category = Objects.requireNonNull(builder.category, "category");
        this.difficulty = Objects.requireNonNull(builder.difficulty, "difficulty");
        this.sheetName = requireText(builder.sheetName, "sheetName");
        this.dataset = builder.dataset;
        this.steps = List.copyOf(builder.steps);
        this.rules = List.copyOf(builder.rules);
        this.workbookFileName = requireText(builder.workbookFileName, "workbookFileName");

        if (rules.isEmpty()) {
            throw new IllegalArgumentException("A task needs at least one validation rule");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public TaskCategory category() {
        return category;
    }

    public TaskDifficulty difficulty() {
        return difficulty;
    }

    public String sheetName() {
        return sheetName;
    }

    public Optional<Dataset> dataset() {
        return Optional.ofNullable(dataset);
    }

    public List<String> steps() {
        return steps;
    }

    public List<ValidationRule> rules() {
        return rules;
    }

    public String workbookFileName() {
        return workbookFileName;
    }

    public CellAddress cell(int row, int column) {
        return CellAddress.of(sheetName, row, column);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    public static final class Builder {
        private String id;
        private String name;
        private String description;
        private TaskCategory category;
        private TaskDifficulty difficulty;
        private String sheetName = "Practice";
        private Dataset dataset;
        private final List<String> steps = new ArrayList<>();
        private final List<ValidationRule> rules = new ArrayList<>();
        private String workbookFileName;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(TaskCategory category) {
            this.category = category;
            return this;
        }

        public Builder difficulty(TaskDifficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public Builder dataset(Dataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public Builder step(String step) {
            this.steps.add(step);
            return this;
        }

        public Builder rule(ValidationRule rule) {
            this.rules.add(rule);
            return this;
        }

        public Builder workbookFileName(String fileName) {
            this.workbookFileName = fileName;
            return this;
        }

        public ExcelTask build() {
            return new ExcelTask(this);
        }
    }
}
