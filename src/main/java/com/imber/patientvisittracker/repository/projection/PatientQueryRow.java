package com.imber.patientvisittracker.repository.projection;

import java.time.Instant;

public record PatientQueryRow(
    Long patientId,
    String patientFirstName,
    String patientLastName,
    Long totalCount,
    Long doctorId,
    String doctorFirstName,
    String doctorLastName,
    String doctorTimezone,
    Integer doctorTotalPatients,
    Instant visitStart,
    Instant visitEnd
) {
}