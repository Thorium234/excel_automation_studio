# Excel Automation Studio

**A JavaFX Desktop Application for Excel Practice, Automation and Task Validation**

| Project Information | Details |
|---|---|
| Project Name | Excel Automation Studio |
| Language | Java 21 |
| Desktop Framework | JavaFX |
| Build Tool | Maven |
| Desktop Interaction | Java AWT Robot |
| Spreadsheet Validation | Apache POI |
| UI Design | JavaFX FXML + CSS |
| Storage | Local filesystem |
| Future Database | SQLite (optional) |
| Current Version | 0.1.0 |
| Development Status | Planning |

---

## 1. Project Overview

Excel Automation Studio is a Java desktop application designed to automate, execute and validate practical spreadsheet exercises.

The application provides a graphical interface where users can select Excel exercises, configure session durations, execute spreadsheet operations, monitor progress and review completed work.

The application will interact with an installed desktop spreadsheet application through controlled mouse and keyboard operations.

It will support genuine spreadsheet operations including:

- Entering and editing spreadsheet data.
- Navigating worksheets.
- Selecting individual cells and ranges.
- Entering and evaluating formulas.
- Deleting cell contents.
- Inserting and removing rows and columns.
- Resizing columns and rows.
- Formatting worksheets.
- Sorting and filtering datasets.
- Creating charts.
- Saving and validating workbooks.

The project will prioritize reliability, modularity, maintainability and predictable execution.

**Important:** The application is a productivity, practice and testing tool. Recording an action in another application does not establish that the action qualifies for any external platform's work classification or payment criteria.

## 2. Project Objectives

The primary objectives are:

1. Build a reliable JavaFX desktop application.
2. Implement independent mouse and keyboard controllers.
3. Develop an extensible Excel task engine.
4. Support actual desktop spreadsheet interaction.
5. Generate realistic spreadsheet datasets.
6. Execute practical Excel exercises.
7. Validate formulas and spreadsheet results.
8. Provide configurable session durations.
9. Implement safe Start, Pause, Resume and Stop operations.
10. Maintain execution logs and task history.
11. Save completed workbooks locally.
12. Keep the architecture ready for future programming automation.

## 3. Technology Stack

| Technology | Responsibility |
|---|---|
| Java 21 | Core programming language |
| JavaFX | Desktop user interface |
| Maven | Dependency management and build |
| JavaFX FXML | Interface layouts |
| JavaFX CSS | Application styling |
| java.awt.Robot | Mouse and keyboard interaction |
| Apache POI | Workbook creation, inspection and validation |
| JUnit 5 | Unit and integration testing |
| SLF4J + Logback | Application logging |
| SQLite | Optional future persistence |

### Technology rules

- Use Java 21 as the minimum project target.
- Use Maven for dependency and build management.
- Keep JavaFX UI code separate from automation logic.
- Do not use Spring Boot.
- Do not introduce Redis.
- Do not introduce microservices.
- Do not introduce a web server.
- Do not add external services when local functionality is sufficient.
- Do not duplicate functionality already provided by Java or JavaFX.

## 4. Project Structure

The following is the intended project structure.

```text
excel-automation-studio/
│
├── pom.xml
├── README.md
├── .gitignore
│
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DEVELOPMENT_PLAN.md
│   ├── TECHNICAL_SPEC.md
│   └── TESTING.md
│
├── src/
│   │
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── excelstudio/
│   │   │
│   │   │           ├── MainApp.java
│   │   │           │
│   │   │           ├── controller/
│   │   │           │   ├── DashboardController.java
│   │   │           │   ├── ExcelController.java
│   │   │           │   └── SettingsController.java
│   │   │
│   │   │           ├── automation/
│   │   │           │   ├── DesktopController.java
│   │   │           │   ├── MouseController.java
│   │   │           │   ├── KeyboardController.java
│   │   │           │   ├── ScreenController.java
│   │   │           │   └── WindowController.java
│   │   │
│   │   │           ├── excel/
│   │   │           │   ├── ExcelTaskEngine.java
│   │   │           │   ├── ExcelTask.java
│   │   │           │   ├── TaskGenerator.java
│   │   │           │   ├── TaskExecutor.java
│   │   │           │   ├── TaskValidator.java
│   │   │           │   ├── FormulaEngine.java
│   │   │           │   └── DatasetGenerator.java
│   │   │
│   │   │           ├── session/
│   │   │           │   ├── SessionManager.java
│   │   │           │   ├── SessionConfig.java
│   │   │           │   └── SessionState.java
│   │   │
│   │   │           ├── model/
│   │   │           │   ├── TaskResult.java
│   │   │           │   ├── SessionStats.java
│   │   │           │   ├── TaskDifficulty.java
│   │   │           │   └── TaskCategory.java
│   │   │
│   │   │           ├── service/
│   │   │           │   ├── WorkbookService.java
│   │   │           │   ├── LoggingService.java
│   │   │           │   └── FileService.java
│   │   │
│   │   │           ├── config/
│   │   │           │   └── AppConfig.java
│   │   │
│   │   │           └── util/
│   │   │               ├── ProcessManager.java
│   │   │               └── ValidationUtils.java
│   │   │
│   │   └── resources/
│   │       │
│   │       ├── fxml/
│   │       │   ├── dashboard.fxml
│   │       │   ├── excel.fxml
│   │       │   └── settings.fxml
│   │       │
│   │       ├── css/
│   │       │   └── app.css
│   │       │
│   │       └── images/
│   │
│   └── test/
│       │
│       └── java/
│           └── com/
│               └── excelstudio/
│                   ├── automation/
│                   ├── excel/
│                   ├── session/
│                   └── service/
│
├── data/
│   ├── datasets/
│   ├── workbooks/
│   └── reports/
│
└── logs/
```

Directories should be created as their corresponding features are implemented. Do not create empty classes simply to make the directory tree look complete.

## 5. Core Modules

### 5.1 Mouse Controller

File: `automation/MouseController.java`

Responsible for controlled desktop mouse interaction.

Required capabilities:

- Move pointer to screen coordinates.
- Click at specified coordinates.
- Single-click.
- Double-click.
- Right-click.
- Drag and drop.
- Scroll vertically.
- Retrieve current pointer position.
- Validate screen boundaries.
- Support cancellation between actions.

The controller must not contain Excel-specific business logic.

### 5.2 Keyboard Controller

File: `automation/KeyboardController.java`

Responsible for controlled keyboard interaction.

Required capabilities:

- Type text.
- Press individual keys.
- Release individual keys.
- Execute keyboard shortcuts.
- Navigate using arrow keys.
- Use Tab and Enter.
- Support Delete and Backspace.
- Handle key combinations.
- Release pressed keys safely.
- Support cancellation.

The controller must handle unsupported characters and keyboard layouts safely.

### 5.3 Desktop Controller

File: `automation/DesktopController.java`

Acts as the common interface for desktop automation.

Responsibilities:

- Coordinate mouse and keyboard operations.
- Manage automation availability.
- Check application interaction readiness.
- Coordinate cancellation.
- Handle desktop interaction failures.
- Provide consistent error reporting.

It must not implement Excel formulas or task-generation logic.

### 5.4 Excel Tasks Engine

File: `excel/ExcelTaskEngine.java`

This is the primary business logic module.

Responsibilities:

- Manage Excel exercises.
- Select appropriate tasks.
- Generate datasets.
- Execute task definitions.
- Track task progress.
- Validate task completion.
- Record results.

The engine must not contain JavaFX UI logic.

### 5.5 Task Generator

File: `excel/TaskGenerator.java`

Responsible for generating exercise definitions.

Tasks should have:

- Unique ID.
- Task name.
- Description.
- Difficulty.
- Category.
- Dataset requirements.
- Required spreadsheet operations.
- Expected results.
- Validation rules.

Random task selection must use defined categories and difficulty levels.

Randomization must never produce invalid or impossible exercises.

### 5.6 Dataset Generator

File: `excel/DatasetGenerator.java`

Responsible for generating realistic practice datasets.

Supported dataset types:

- Employee records.
- Sales transactions.
- Inventory records.
- Student records.
- Expenses.
- Attendance records.
- Customer records.
- Product information.

Generated data must have consistent columns, valid data types and predictable relationships.

### 5.7 Formula Engine

File: `excel/FormulaEngine.java`

Responsible for defining and managing formula exercises.

Initial supported functions:

**Mathematical**
- SUM
- AVERAGE
- MIN
- MAX
- ROUND

**Logical**
- IF
- AND
- OR

**Counting**
- COUNT
- COUNTA
- COUNTIF
- COUNTIFS

**Lookup**
- VLOOKUP
- XLOOKUP, where supported by the installed spreadsheet application.

**Text**
- LEFT
- RIGHT
- MID
- CONCAT
- TRIM
- LEN

**Date**
- TODAY
- DATE
- YEAR
- MONTH
- DAY

Formula evaluation must be verified against the actual spreadsheet application's calculated results. Apache POI must not be treated as a complete replacement for Excel's calculation engine.

### 5.8 Task Validator

File: `excel/TaskValidator.java`

Responsible for determining whether an exercise has been completed correctly.

Validation should include:

- Workbook existence.
- Worksheet existence.
- Expected cell values.
- Formula presence.
- Calculated values.
- Required formatting.
- Dataset completeness.
- Expected row and column counts.

A task must not be marked successful simply because an automation action was executed.

## 6. Excel Task Categories

The initial task engine should support the following categories.

| Category | Exercises |
|---|---|
| Data Entry | Enter names, dates, numbers and text |
| Cell Navigation | Select cells and ranges |
| Editing | Modify existing cell values |
| Deletion | Clear cells and remove rows or columns |
| Formulas | Enter and calculate formulas |
| Formatting | Fonts, colors, borders and alignment |
| Column Management | Resize and adjust columns |
| Row Management | Resize and adjust rows |
| Sorting | Sort records by selected columns |
| Filtering | Filter records by criteria |
| Data Cleaning | Identify duplicates and invalid records |
| Charts | Create basic spreadsheet charts |
| Worksheets | Create, rename and delete worksheets |
| Workbook Management | Save and organize workbooks |

### Difficulty levels

**Basic**
- Data entry.
- Cell navigation.
- Basic calculations.
- Simple formatting.

**Intermediate**
- IF statements.
- COUNTIF and SUMIF.
- Sorting and filtering.
- Data cleaning.
- Multiple worksheets.

**Advanced**
- Lookup functions.
- Conditional formatting.
- Complex formulas.
- Charts and summaries.
- Combined spreadsheet exercises.

## 7. Session Management

The session manager controls the duration and execution lifecycle of a practice session.

File: `session/SessionManager.java`

### Duration presets

- 30 minutes.
- 1 hour.
- 3 hours.
- 5 hours.
- 8 hours.
- 10 hours.
- Custom duration.

Custom duration must support hours and minutes.

### Session controls

- Start.
- Pause.
- Resume.
- Stop.
- Complete.

### Session states

```text
IDLE
  |
  v
RUNNING
  |
  +------> PAUSED
  |          |
  |          v
  |        RUNNING
  |
  +------> COMPLETED
  |
  +------> STOPPED
  |
  +------> FAILED
```

### Session requirements

- Maintain accurate elapsed time.
- Track remaining time.
- Support pause and resume.
- Preserve task checkpoints.
- Support graceful cancellation.
- Save session results.
- Prevent duplicate session execution.
- Handle application shutdown safely.

The 10-hour preset is a session-duration option, not a guarantee of external platform eligibility. Loggy3's FAQ separately describes a maximum of 10 eligible recording hours per UTC day.

## 8. Desktop Interface

The application should use a clean, professional desktop design.

### Dashboard

The dashboard should display:

- Application name.
- Excel automation module.
- Future programming module.
- Session duration.
- Current task.
- Session progress.
- Completed exercises.
- Current session status.
- Execution logs.

### Excel screen

The Excel screen should display:

- Available task categories.
- Difficulty selection.
- Task description.
- Dataset information.
- Execution status.
- Validation results.
- Saved workbook location.

### Settings screen

Settings should include:

- Desktop interaction configuration.
- Spreadsheet application selection.
- Output directory.
- Session duration defaults.
- Logging preferences.
- Emergency stop configuration.

### Design requirements

- Responsive desktop layout.
- Light theme by default.
- Consistent spacing.
- Clear typography.
- No unnecessary animations.
- No black-background dashboard.
- Accessible Start, Pause and Stop controls.
- Clear error messages.

## 9. Execution and Safety Rules

These rules apply throughout the project.

1. Never execute an infinite task loop.
2. Never allow an automation thread to freeze the JavaFX interface.
3. Always support cancellation.
4. Release all pressed keys and mouse buttons after failures.
5. Do not perform destructive operations on user-selected workbooks without explicit authorization.
6. Use dedicated practice workbooks by default.
7. Never overwrite existing files without permission.
8. Verify workbook paths before saving.
9. Handle unexpected dialogs safely.
10. Record errors and failed tasks.
11. Do not claim task completion without validation.
12. Do not disguise automated actions as human participation or attempt to evade external monitoring.

### Threading

JavaFX UI updates must execute on the JavaFX Application Thread.

Desktop automation and workbook processing must run on background executors.

The UI must remain responsive during long-running tasks.

## 10. Development Roadmap

Development must follow the phases below.

### Phase 1: Project Foundation

**Objective:** Establish a working JavaFX Maven application.

Deliverables:

- Maven project.
- JavaFX configuration.
- Main application entry point.
- Dashboard.
- Navigation.
- CSS styling.
- Logging setup.
- Initial project documentation.

Acceptance criteria:

- Maven builds successfully.
- Application launches.
- Dashboard displays correctly.
- Navigation works.
- No unnecessary dependencies.

### Phase 2: Mouse Controller

**Objective:** Implement and test mouse interaction independently.

Deliverables:

- Mouse movement.
- Click operations.
- Double-click.
- Right-click.
- Drag and drop.
- Scroll.
- Position retrieval.
- Boundary validation.
- Cancellation.

Acceptance criteria:

- Each operation has an isolated test.
- Invalid coordinates are rejected.
- Cancellation works.
- No stuck mouse buttons.

### Phase 3: Keyboard Controller

**Objective:** Implement and test keyboard interaction independently.

Deliverables:

- Text entry.
- Key presses.
- Key releases.
- Keyboard shortcuts.
- Navigation keys.
- Key combinations.
- Error handling.

Acceptance criteria:

- Key presses are released correctly.
- Unsupported input is handled.
- Cancellation works.
- No stuck keys.

### Phase 4: Desktop Controller Integration

**Objective:** Integrate the mouse and keyboard controllers.

Deliverables:

- Unified desktop interface.
- Interaction coordination.
- Cancellation handling.
- Error reporting.
- Application readiness checks.

Acceptance criteria:

- Mouse and keyboard controllers work independently and together.
- Failure in one operation does not crash the application.
- Desktop interaction can be stopped safely.

### Phase 5: Excel Task Engine

**Objective:** Build the exercise generation and execution foundation.

Deliverables:

- ExcelTask model.
- TaskGenerator.
- DatasetGenerator.
- Task categories.
- Difficulty levels.
- Formula definitions.
- Task execution interface.

Acceptance criteria:

- Tasks are generated correctly.
- Datasets contain valid information.
- Task definitions are reusable.
- Task generation is independent of JavaFX.

### Phase 6: Excel Desktop Interaction

**Objective:** Implement genuine spreadsheet operations.

Deliverables:

- Open spreadsheet application.
- Create practice workbook.
- Enter spreadsheet data.
- Select cells and ranges.
- Edit cell contents.
- Delete cell contents.
- Resize rows and columns.
- Insert and remove rows and columns.
- Enter formulas.
- Apply formatting.
- Save workbooks.

Acceptance criteria:

- Operations produce the expected spreadsheet changes.
- Unexpected application states are handled.
- Destructive operations require appropriate authorization.
- Workbooks are saved correctly.

### Phase 7: Formula and Result Validation

**Objective:** Verify that exercises produce correct results.

Deliverables:

- Formula verification.
- Cell value validation.
- Dataset validation.
- Workbook inspection.
- Task completion reporting.

Acceptance criteria:

- Incorrect formulas are detected.
- Missing data is detected.
- Incorrect results are rejected.
- Completed exercises have verifiable results.

### Phase 8: Session Management

**Objective:** Integrate session configuration and execution controls.

Deliverables:

- Duration presets.
- Custom duration.
- Start.
- Pause.
- Resume.
- Stop.
- Progress tracking.
- Session statistics.

Acceptance criteria:

- Accurate timer.
- Safe pause and resume.
- No duplicate execution.
- Graceful shutdown.

### Phase 9: Reporting and History

**Objective:** Provide a record of completed practice.

Deliverables:

- Task history.
- Session summaries.
- Error reports.
- Workbook output locations.
- Completion statistics.

Acceptance criteria:

- Reports reflect actual task results.
- Failed tasks are not marked successful.
- Previous sessions can be reviewed.

### Phase 10: Programming Automation

**Objective:** Introduce a separate programming practice module.

This phase is not part of the initial MVP.

Future capabilities:

- Terminal interaction.
- Code exercise generation.
- Source file creation.
- Language selection.
- Compilation and execution.
- Test validation.
- Execution logs.

Potential languages:

- Java.
- Python.
- C.
- Go.

Programming automation must remain independent of the Excel engine.

## 11. Testing Strategy

Every major component must have tests.

| Component | Required tests |
|---|---|
| MouseController | Movement, clicks, drag, scroll |
| KeyboardController | Typing, key presses, shortcuts |
| DesktopController | Coordination and cancellation |
| TaskGenerator | Valid task generation |
| DatasetGenerator | Valid and consistent datasets |
| FormulaEngine | Formula definitions |
| TaskValidator | Correct and incorrect results |
| SessionManager | Timer, pause, resume and stop |
| WorkbookService | Saving and workbook inspection |

Use JUnit 5 for unit tests.

Desktop interaction tests should use a controlled test environment and a dedicated practice workbook.

## 12. Build and Run

### Prerequisites

- Java Development Kit 21.
- Apache Maven.
- An installed desktop spreadsheet application.
- Supported desktop graphical environment.

### Verify Java

```bash
java -version
```

### Verify Maven

```bash
mvn -version
```

### Build

```bash
mvn clean package
```

### Run

```bash
mvn javafx:run
```

The Maven configuration must provide the JavaFX runtime and required platform dependencies.

## 13. Project Development Rules

These rules are mandatory for anyone contributing to the project, including AI coding agents.

### Scope control

- Implement only the currently approved development phase.
- Do not skip phases.
- Do not implement future modules prematurely.
- Do not introduce features that are not in the specification.
- Do not replace existing architecture without justification.

### Code quality

- Use clear class and method names.
- Follow Java naming conventions.
- Keep classes focused on one responsibility.
- Avoid unnecessarily large classes.
- Handle exceptions explicitly.
- Avoid duplicated code.
- Use interfaces where they provide a genuine architectural benefit.

### Dependency control

- Review every proposed dependency.
- Prefer standard Java APIs where sufficient.
- Do not introduce heavyweight frameworks unnecessarily.
- Keep Maven dependencies minimal.

### Testing

- Write tests alongside implementation.
- Do not claim a feature is complete without testing it.
- Fix compilation errors before implementing additional features.
- Do not ignore failing tests.

### Completion reporting

At the end of each phase, provide:

1. Files created.
2. Files modified.
3. Features implemented.
4. Dependencies added.
5. Tests executed.
6. Test results.
7. Known limitations.
8. Remaining work.

## 14. Initial MVP Scope

The first usable release should contain:

- JavaFX dashboard.
- JavaFX navigation.
- MouseController.
- KeyboardController.
- DesktopController.
- ExcelTask model.
- TaskGenerator.
- DatasetGenerator.
- Basic formula exercises.
- Excel desktop interaction.
- Cell editing and deletion.
- Row and column manipulation.
- Workbook saving.
- Formula validation.
- Basic session management.
- Execution logs.

The programming module, cloud services, AI integration and advanced reporting are outside the initial MVP.

## 15. Definition of Done

The initial MVP is considered complete when:

- The application builds using Maven.
- The JavaFX desktop interface launches.
- Mouse and keyboard controllers pass their tests.
- The application can create and interact with a dedicated practice workbook.
- Real spreadsheet data can be entered and modified.
- Formulas can be entered and verified.
- Cell contents can be deleted.
- Rows and columns can be manipulated.
- Workbooks can be saved.
- Tasks can be validated.
- Session controls operate reliably.
- Errors are logged.
- No critical tests are failing.

---

## 16. Final Development Principle

**Build a reliable desktop automation application, not a collection of unrelated scripts.**

The mouse controller must remain independent of the Excel engine.

The keyboard controller must remain independent of the Excel engine.

The Excel engine must define tasks without depending on the desktop interface.

The desktop controller coordinates input operations.

The session manager controls execution.

The task validator determines whether work was completed correctly.

Every phase must leave the project in a working, testable state.

No unnecessary features. No speculative architecture. No skipping tests. No pretending unfinished functionality is complete.
