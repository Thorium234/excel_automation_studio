# Architecture

Excel Automation Studio keeps the user interface, automation primitives and Excel
logic separate so each part can be tested on its own.

## Layers

```text
Controller (JavaFX)
    |
    v
AppServices (wiring, background worker, session and report state)
    |
    +--> Excel engine (TaskGenerator, ExcelTaskEngine, PoiTaskValidator)
    |         |
    |         +--> WorkbookService (Apache POI file access)
    |
    +--> SessionManager (lifecycle and monotonic timing)
    |
    +--> ReportService (Markdown session reports)
    |
    +--> Desktop automation (MouseController, KeyboardController,
              ScreenController, DesktopController, ExcelDesktopAutomation)
```

### UI package

- `com.excelstudio.controller` contains FXML controllers only.
- `com.excelstudio.ui` contains navigation (`View`, `ViewNavigator`) and the
  `Refreshable` lifecycle hook.
- Controllers never perform workbook work on the JavaFX thread. Long operations
  are submitted to `AppServices.workers()` and their results are returned through
  `Platform.runLater`.

### Excel package

- `ExcelTask` is immutable data: name, category, difficulty, dataset, steps and
  validation rules. It contains no JavaFX or robot code.
- `TaskGenerator` derives expected values from the generated dataset, so a task
  cannot expect a value the dataset does not contain.
- `TaskExecutor` and `TaskValidator` are interfaces. `ExcelTaskEngine` runs one
  then the other and only reports `PASSED` when validation passes.
- `WorkbookTaskExecutor` performs the deterministic practice work with POI.
- `PoiTaskValidator` re-opens the saved file and checks every rule.
- `ExcelDesktopAutomation` exposes live spreadsheet keystrokes for a real
  spreadsheet application. References are validated before any key is sent.

### Automation package

- `RobotBackend` isolates `java.awt.Robot`; `FakeRobotBackend` is used by tests.
- `DesktopController` owns cancellation and guarantees that pressed keys and
  mouse buttons are released on cancellation, failure or close.

### Session and reporting

- `SessionManager` uses an injectable monotonic ticker, so tests control time.
- A second `start()` is rejected; pause/resume never lose elapsed time.
- `ReportService` writes a new Markdown file for every finished session and never
  overwrites an existing report.

## Threading rules

1. The JavaFX thread only updates controls.
2. Workbook creation, execution and validation run on the worker executor.
3. Session state is guarded by `synchronized` methods.
4. Execution logs use a thread-safe list.
5. Shutdown stops an active session and waits briefly for worker completion.

## Safety rules

- No task is marked complete without a passing validation result.
- Practice workbooks get unique paths; existing files are not overwritten.
- Live automation validates cell/range references before typing.
- Cancellation releases all pressed keys and mouse buttons.
- No infinite task loop exists: one task is generated and run per request.
