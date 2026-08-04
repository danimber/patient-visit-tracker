package com.imber.patientvisittracker.service;

import com.imber.patientvisittracker.dto.response.DoctorSummaryDto;
import com.imber.patientvisittracker.dto.response.PatientDto;
import com.imber.patientvisittracker.dto.response.PatientListResponse;
import com.imber.patientvisittracker.dto.response.PatientVisitDto;
import com.imber.patientvisittracker.repository.PatientQueryRepository;
import com.imber.patientvisittracker.repository.projection.LastVisitRow;
import com.imber.patientvisittracker.repository.projection.PatientPageResult;
import com.imber.patientvisittracker.repository.projection.PatientRow;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PatientQueryService {

    private final PatientQueryRepository repository;

    public PatientQueryService(PatientQueryRepository repository) {
        this.repository = repository;
    }

    public PatientListResponse findPatients(String search, List<Long> doctorIds, int page, int size) {
        PatientPageResult patientPage = repository.findPatientPage(search, doctorIds, page, size);

        if (patientPage.patients().isEmpty()) {
            return new PatientListResponse(List.of(), patientPage.totalCount());
        }

        List<Long> patientIds = patientPage.patients().stream().map(PatientRow::id).toList();

        List<LastVisitRow> lastVisits = repository.findLastVisits(patientIds, doctorIds);

        Set<Long> involvedDoctorIds = lastVisits.stream()
                .map(LastVisitRow::doctorId)
                .collect(Collectors.toSet());
        Map<Long, Integer> totalPatientsByDoctor = repository.findTotalPatientsByDoctor(involvedDoctorIds);

        Map<Long, List<LastVisitRow>> visitsByPatient = lastVisits.stream()
                .collect(Collectors.groupingBy(LastVisitRow::patientId, LinkedHashMap::new, Collectors.toList()));

        List<PatientDto> patientDtos = patientPage.patients().stream()
                .map(row -> toPatientDto(row, visitsByPatient.getOrDefault(row.id(), List.of()), totalPatientsByDoctor))
                .toList();

        return new PatientListResponse(patientDtos, patientPage.totalCount());
    }

    private PatientDto toPatientDto(PatientRow patient,
                                     List<LastVisitRow> visits,
                                     Map<Long, Integer> totalPatientsByDoctor) {
        List<PatientVisitDto> visitDtos = visits.stream()
                .map(v -> {
                    ZoneId doctorZone = ZoneId.of(v.doctorTimezone());
                    return new PatientVisitDto(
                            LocalDateTime.ofInstant(v.start(), doctorZone).toString(),
                            LocalDateTime.ofInstant(v.end(), doctorZone).toString(),
                            new DoctorSummaryDto(
                                    v.doctorFirstName(),
                                    v.doctorLastName(),
                                    totalPatientsByDoctor.getOrDefault(v.doctorId(), 0)
                            )
                    );
                })
                .toList();

        return new PatientDto(patient.firstName(), patient.lastName(), visitDtos);
    }
}
