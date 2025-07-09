// exception/JobCalendarException.java
package com.example.demo.exception;

public class JobCalendarException extends RuntimeException {
    private final String errorCode;

    public JobCalendarException(String message) {
        super(message);
        this.errorCode = "JOB_CALENDAR_ERROR";
    }

    public JobCalendarException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}