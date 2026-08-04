package com.imber.patientvisittracker.exception;

public class OverlappingVisitException extends RuntimeException {
    public OverlappingVisitException(String message) {
        super(message);
    }
}
