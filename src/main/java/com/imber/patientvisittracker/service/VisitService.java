package com.imber.patientvisittracker.service;

import com.imber.patientvisittracker.dto.request.CreateVisitRequest;
import com.imber.patientvisittracker.dto.response.VisitResponse;
import com.imber.patientvisittracker.entity.Doctor;
import com.imber.patientvisittracker.entity.Patient;
import com.imber.patientvisittracker.entity.Visit;
import com.imber.patientvisittracker.exception.OverlappingVisitException;
import com.imber.patientvisittracker.exception.ResourceNotFoundException;
import com.imber.patientvisittracker.repository.DoctorRepository;
import com.imber.patientvisittracker.repository.PatientRepository;
import com.imber.patientvisittracker.repository.VisitRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@Service
@AllArgsConstructor
public class VisitService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    @Transactional
    public VisitResponse createVisit(CreateVisitRequest request) {
        Doctor doctor = doctorRepository.findByIdForUpdate(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor with id %d not found".formatted(request.doctorId())));

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient with id %d not found".formatted(request.patientId())));

        ZoneId doctorZone = resolveZone(doctor.getTimezone());

        Instant start = toInstant(request.start(), doctorZone, "start");
        Instant end = toInstant(request.end(), doctorZone, "end");

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("'end' must be strictly after 'start'");
        }

        if (visitRepository.existsOverlapping(doctor.getId(), start, end)) {
            throw new OverlappingVisitException(
                    "Doctor %d already has a visit overlapping the requested time slot".formatted(doctor.getId()));
        }

        Visit saved = visitRepository.save(new Visit(start, end, patient, doctor));

        return VisitResponse.from(saved, doctorZone);
    }

    private ZoneId resolveZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            throw new IllegalStateException("Doctor has an invalid timezone configured: " + timezone, e);
        }
    }

    private Instant toInstant(String value, ZoneId zone, String fieldName) {
        try {
            return LocalDateTime.parse(value).atZone(zone).toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "'%s' must be an ISO-8601 local date-time, e.g. 2026-08-05T10:00:00".formatted(fieldName));
        }
    }
}
