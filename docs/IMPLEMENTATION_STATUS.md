# Implementation status

This file records what has been implemented for each roadmap phase, how it is
verified, and what remains deliberately out of scope.

## Phase summary

| Phase | Scope | Status |
|---|---|---|
| 1 | Maven project, JavaFX shell, navigation, logging, settings | Complete |
| 2 | `MouseController` | Complete |
| 3 | `KeyboardController` | Complete |
| 4 | `DesktopController`, screen coordination, cancellation | Complete |
| 5 | Excel task engine (models, datasets, formulas, validation) | Complete |
| 6 | Excel desktop interaction (`ExcelDesktopAutomation`) | Complete, live GUI untested |
| 7 | Formula and result validation (`PoiTaskValidator`) | Complete |
| 8 | Session management (`SessionManager`, `SessionConfig`) | Complete |
| 9 | Reporting and history (`ReportService`, history screen) | Complete |
| 10 | Programming automation | Not part of the MVP; not implemented |

## Verification

Verification is limited by the build environment: Maven cannot reach Maven Central,
and no spreadsheet application is installed. Compilation and tests are therefore run
with `javac` and the JUnit Platform directly against the locally cached artifacts.

Current suite: 115 tests, all passing. The suite includes:

- Mouse, keyboard, screen and desktop-controller tests using a fake robot backend.
- Dataset, formula, task-generator and address tests.
- An end-to-end pipeline test that generates, executes and validates all
  14 categories at all three difficulties (42 cases) with Apache POI.
- Validator tests that deliberately corrupt a workbook and confirm failure.
- Session lifecycle tests with an injected monotonic clock.
- Report tests, including no-overwrite behaviour and history listing.
- FXML contract tests, so every `fx:id` and `onAction` matches its controller.

## Phase 1: Foundation

- JavaFX shell (`dashboard.fxml`) with `ViewNavigator` and cached content views
  (`home`, `excel`, `history`, `settings`).
- CSS light theme, rolling Logback configuration, SLF4J logging.
- `AppConfig` persists settings in `~/.excel-automation-studio/config.properties`.
- `MainApp` initialises shared services and shuts them down on exit.

## Phases 2-4: Desktop automation

- `RobotBackend` abstraction with `AwtRobotBackend` for production.
- `MouseController`: move, click, double click, drag, scroll.
- `KeyboardController`: typing, taps, chords/shortcuts, releases keys on failure.
- `ScreenController`: screen bounds and capture helpers.
- `DesktopController`: coordinates the three controllers, supports cancellation and
  always releases pressed keys and buttons.

## Phase 5: Excel task engine

- Models: `TaskCategory`, `TaskDifficulty`, `TaskStatus`, `CellAddress`,
  `ValidationType`, `ValidationRule`, `ValidationResult`, `TaskResult`.
- `Dataset` with validated, immutable rows; `DatasetGenerator` for eight dataset
  families with deterministic seeds.
- `FormulaEngine` catalogue plus reference builders for SUM, AVERAGE, COUNTIF,
  SUMIF, VLOOKUP, text and date functions.
- `TaskGenerator` covers all 14 categories and three difficulties; expected values
  are derived from the generated dataset.
- `TaskValidator`/`TaskExecutor` interfaces, `ExcelTaskEngine` orchestration.

## Phase 6: Excel desktop interaction

- `ExcelDesktopAutomation` drives the focused spreadsheet application:
  Name Box navigation, range selection, typing, clearing, bold, row/column
  sizing, insert/delete row and save.
- Cell and range references are validated before any key is sent, so arbitrary
  text can never be typed into the application by accident.
- Not exercised against a live spreadsheet; the key sequence is verified through
  the fake robot backend.

## Phase 7: Validation

- `PoiTaskValidator` implements every `ValidationType`: readable workbook, sheet
  presence, cell value/empty/formula/bold, column width, row height, sorted column
  values and chart presence.
- Numbers use a small tolerance; formulas are normalised before comparison so
  `=SUM(E2:E9)` and `=sum( $E$2:$E$9 )` match.
- `WorkbookTaskExecutor` performs the practice work with POI, saves a real `.xlsx`
  file, and the validator re-opens it. A task is never marked passed without a
  passing validation result.

## Phase 8: Sessions

- Presets: 30 minutes, 1, 3, 5, 8 and 10 hours, plus custom hours/minutes.
- Start, pause, resume, stop and complete with explicit states.
- Monotonic elapsed time; remaining time never goes negative; duplicate start is
  rejected; listeners cannot break session control.
- Session results are recorded only while the session runs.
- Shutdown stops an active session safely.

## Phase 9: Reporting and history

- Every completed or stopped session writes a Markdown report under
  `data/reports/` next to the workbook output directory.
- Reports include state, planned/elapsed/remaining time, completion statistics,
  task status, duration, workbook location and validation details.
- Existing reports are never overwritten.
- The History screen lists reports newest first, previews the selected report and
  opens the reports folder.

## Known limitations

- Apache POI is used for deterministic practice workbooks. It is not treated as
  Excel's calculation engine; formula values are also checked after the workbook
  has been saved and re-opened.
- Live desktop automation against Excel/LibreOffice is implemented but not
  covered by automated tests in this environment.
- Maven dependency resolution is blocked here, so the suite is executed with
  `javac` plus the JUnit Platform launcher against cached jars. `mvn test` should
  be re-run in a networked environment before release.
- Programming automation (Phase 10) is intentionally absent from the MVP.
