package com.imber.patientvisittracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateVisitRequest(
        @NotBlank(message = "start is required") String start,
        @NotBlank(message = "end is required") String end,
        @NotNull(message = "patientId is required") Long patientId,
        @NotNull(message = "doctorId is required") Long doctorId
) {
}
