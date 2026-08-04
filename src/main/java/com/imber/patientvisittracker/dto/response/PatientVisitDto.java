package com.imber.patientvisittracker.dto.response;

public record PatientVisitDto(String start, String end, DoctorSummaryDto doctor) {
}
