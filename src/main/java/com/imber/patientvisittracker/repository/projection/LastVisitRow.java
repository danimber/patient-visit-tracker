package com.imber.patientvisittracker.repository.projection;

import java.time.Instant;

public record LastVisitRow(
        Long patientId,
        Long doctorId,
        String doctorFirstName,
        String doctorLastName,
        String doctorTimezone,
        Instant start,
        Instant end
) {
}
