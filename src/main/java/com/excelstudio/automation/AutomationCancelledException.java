package com.excelstudio.automation;

/**
 * Signals that an automation operation stopped because cancellation was requested.
 */
public class AutomationCancelledException extends AutomationException {
    public AutomationCancelledException(String message) {
        super(message);
    }
}
