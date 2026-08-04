package com.imber.patientvisittracker.dto.response;

import com.imber.patientvisittracker.entity.Visit;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record VisitResponse(Long id, String start, String end, Long patientId, Long doctorId) {

    public static VisitResponse from(Visit visit, ZoneId doctorZone) {
        return new VisitResponse(
                visit.getId(),
                LocalDateTime.ofInstant(visit.getStartDateTime(), doctorZone).toString(),
                LocalDateTime.ofInstant(visit.getEndDateTime(), doctorZone).toString(),
                visit.getPatient().getId(),
                visit.getDoctor().getId()
        );
    }
}
