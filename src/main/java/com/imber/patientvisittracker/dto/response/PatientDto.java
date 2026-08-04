package com.imber.patientvisittracker.dto.response;

import java.util.List;

public record PatientDto(String firstName, String lastName, List<PatientVisitDto> lastVisits) {
}
