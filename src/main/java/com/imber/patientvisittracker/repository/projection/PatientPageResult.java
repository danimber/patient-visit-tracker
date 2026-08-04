package com.imber.patientvisittracker.repository.projection;

import java.util.List;

public record PatientPageResult(List<PatientRow> patients, long totalCount) {
}
