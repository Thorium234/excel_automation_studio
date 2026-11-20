package com.excelstudio.automation;

/**
 * Raised when a desktop automation operation cannot be performed safely.
 */
public class AutomationException extends RuntimeException {
    public AutomationException(String message) {
        super(message);
    }

    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }
}
