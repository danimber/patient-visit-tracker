package com.imber.patientvisittracker.dto.response;

import java.util.List;

public record PatientListResponse(List<PatientDto> data, long count) {
}
