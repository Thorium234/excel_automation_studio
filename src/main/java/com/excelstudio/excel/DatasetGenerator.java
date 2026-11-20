package com.excelstudio.excel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.excelstudio.model.TaskDifficulty;

/**
 * Generates consistent practice datasets from fixed column layouts and realistic
 * value pools. The same seed always produces the same dataset.
 */
public final class DatasetGenerator {
    private static final String[] FIRST_NAMES = {
            "Aisha", "Daniel", "Maya", "Liam", "Sofia", "Noah", "Priya", "Omar",
            "Elena", "Kai", "Rosa", "Ethan", "Nadia", "Lucas", "Hana", "Ivan"
    };
    private static final String[] LAST_NAMES = {
            "Khan", "Miller", "Rossi", "Chen", "Novak", "Silva", "Patel", "Haddad",
            "Kowalski", "Tanaka", "Moreau", "Andersson", "Lopez", "Petrov", "Nakamura", "Brown"
    };
    private static final String[] DEPARTMENTS = {
            "Finance", "Operations", "Sales", "Marketing", "Support", "Engineering"
    };
    private static final String[] CITIES = {
            "Berlin", "Lisbon", "Toronto", "Osaka", "Nairobi", "Bogota", "Dublin", "Seoul"
    };
    private static final String[] REGIONS = {"North", "South", "East", "West"};
    private static final String[] PRODUCTS = {
            "Laptop", "Monitor", "Keyboard", "Docking Station", "Headset",
            "Webcam", "Desk Lamp", "Chair", "Standing Desk", "Printer"
    };
    private static final String[] CATEGORIES = {
            "Hardware", "Accessories", "Furniture", "Office", "Electronics"
    };
    private static final String[] EXPENSE_CATEGORIES = {
            "Travel", "Software", "Hardware", "Training", "Office"
    };
    private static final String[] COURSES = {
            "Databases", "Statistics", "Accounting", "Marketing", "Programming", "Logistics"
    };
    private static final String[] ATTENDANCE_STATUS = {"Present", "Remote", "Leave"};
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 2);

    private final Random random;

    public DatasetGenerator() {
        this(new Random());
    }

    public DatasetGenerator(Random random) {
        this.random = random == null ? new Random() : random;
    }

    public Dataset generate(DatasetType type, TaskDifficulty difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("Difficulty must not be null");
        }
        return generate(type, difficulty.datasetRows());
    }

    public Dataset generate(DatasetType type, int rowCount) {
        if (type == null) {
            throw new IllegalArgumentException("Dataset type must not be null");
        }
        if (rowCount < 1 || rowCount > 500) {
            throw new IllegalArgumentException("Row count must be between 1 and 500");
        }
        return switch (type) {
            case EMPLOYEES -> employees(rowCount);
            case SALES -> sales(rowCount);
            case INVENTORY -> inventory(rowCount);
            case STUDENTS -> students(rowCount);
            case EXPENSES -> expenses(rowCount);
            case ATTENDANCE -> attendance(rowCount);
            case CUSTOMERS -> customers(rowCount);
            case PRODUCTS -> products(rowCount);
        };
    }

    private Dataset employees(int rowCount) {
        List<String> columns = List.of("Employee ID", "First Name", "Last Name", "Department", "Salary", "Hire Date");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            rows.add(Dataset.rowOf(
                    "EMP-" + (1000 + i),
                    pick(FIRST_NAMES),
                    pick(LAST_NAMES),
                    pick(DEPARTMENTS),
                    roundTo(35000 + random.nextInt(60_001), 500),
                    date()));
        }
        return new Dataset("Employee records", columns, rows);
    }

    private Dataset sales(int rowCount) {
        List<String> columns = List.of("Order ID", "Order Date", "Product", "Region", "Units", "Unit Price");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            int units = 1 + random.nextInt(40);
            rows.add(Dataset.rowOf(
                    "SO-" + (2000 + i),
                    date(),
                    pick(PRODUCTS),
                    pick(REGIONS),
                    units,
                    round2(5 + random.nextDouble() * 195)));
        }
        return new Dataset("Sales transactions", columns, rows);
    }

    private Dataset inventory(int rowCount) {
        List<String> columns = List.of("SKU", "Product", "Category", "Stock", "Reorder Level", "Unit Cost");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            rows.add(Dataset.rowOf(
                    String.format(Locale.ROOT, "SKU-%04d", 3000 + i),
                    pick(PRODUCTS),
                    pick(CATEGORIES),
                    random.nextInt(501),
                    10 + random.nextInt(91),
                    round2(2 + random.nextDouble() * 198)));
        }
        return new Dataset("Inventory records", columns, rows);
    }

    private Dataset students(int rowCount) {
        List<String> columns = List.of("Student ID", "Name", "Course", "Score", "Enrolled On");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            rows.add(Dataset.rowOf(
                    "STU-" + (4000 + i),
                    pick(FIRST_NAMES) + " " + pick(LAST_NAMES),
                    pick(COURSES),
                    40 + random.nextInt(61),
                    date()));
        }
        return new Dataset("Student records", columns, rows);
    }

    private Dataset expenses(int rowCount) {
        List<String> columns = List.of("Expense ID", "Date", "Category", "Description", "Amount");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            String category = pick(EXPENSE_CATEGORIES);
            rows.add(Dataset.rowOf(
                    "EXP-" + (5000 + i),
                    date(),
                    category,
                    category + " expense " + (i + 1),
                    round2(10 + random.nextDouble() * 890)));
        }
        return new Dataset("Expenses", columns, rows);
    }

    private Dataset attendance(int rowCount) {
        List<String> columns = List.of("Employee", "Date", "Status", "Hours");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            String status = pick(ATTENDANCE_STATUS);
            rows.add(Dataset.rowOf(
                    pick(FIRST_NAMES) + " " + pick(LAST_NAMES),
                    date(),
                    status,
                    "Leave".equals(status) ? 0 : 6 + random.nextInt(3)));
        }
        return new Dataset("Attendance records", columns, rows);
    }

    private Dataset customers(int rowCount) {
        List<String> columns = List.of("Customer ID", "Name", "City", "Email", "Joined", "Orders");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            String first = pick(FIRST_NAMES);
            String last = pick(LAST_NAMES);
            rows.add(Dataset.rowOf(
                    "CUS-" + (6000 + i),
                    first + " " + last,
                    pick(CITIES),
                    (first + "." + last + i + "@example.com").toLowerCase(Locale.ROOT),
                    date(),
                    random.nextInt(26)));
        }
        return new Dataset("Customer records", columns, rows);
    }

    private Dataset products(int rowCount) {
        List<String> columns = List.of("Product ID", "Name", "Category", "Price", "Stock");
        List<List<Object>> rows = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            rows.add(Dataset.rowOf(
                    "PRD-" + (7000 + i),
                    pick(PRODUCTS),
                    pick(CATEGORIES),
                    round2(5 + random.nextDouble() * 495),
                    random.nextInt(401)));
        }
        return new Dataset("Product information", columns, rows);
    }

    private String pick(String[] values) {
        return values[random.nextInt(values.length)];
    }

    private LocalDate date() {
        return START_DATE.plusDays(random.nextInt(365));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static int roundTo(int value, int step) {
        return Math.round((float) value / step) * step;
    }
}
