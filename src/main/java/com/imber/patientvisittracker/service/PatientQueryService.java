package com.imber.patientvisittracker.service;

import com.imber.patientvisittracker.dto.response.DoctorSummaryDto;
import com.imber.patientvisittracker.dto.response.PatientDto;
import com.imber.patientvisittracker.dto.response.PatientListResponse;
import com.imber.patientvisittracker.dto.response.PatientVisitDto;
import com.imber.patientvisittracker.repository.PatientQueryRepository;
import com.imber.patientvisittracker.repository.projection.PatientQueryRow;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientQueryService {

    private final PatientQueryRepository repository;

    public PatientQueryService(PatientQueryRepository repository) {
        this.repository = repository;
    }

    public PatientListResponse findPatients(String search, List<Long> doctorIds, int page, int size) {
        List<PatientQueryRow> rows = repository.findPatients(search, doctorIds, page, size);

        if (rows.isEmpty()) {
            return new PatientListResponse(List.of(), 0);
        }

        Long totalCount = rows.getFirst().totalCount();

        Map<Long, PatientDto> patientsById = new LinkedHashMap<>();
        Map<Long, List<PatientVisitDto>> visitsByPatientId = new LinkedHashMap<>();

        for (PatientQueryRow row : rows) {
            visitsByPatientId.computeIfAbsent(row.patientId(), id -> new ArrayList<>());

            patientsById.computeIfAbsent(row.patientId(), id ->
                new PatientDto(row.patientFirstName(), row.patientLastName(), visitsByPatientId.get(id)));

            if (row.doctorId() != null) {
                ZoneId doctorZone = ZoneId.of(row.doctorTimezone());
                visitsByPatientId.get(row.patientId()).add(new PatientVisitDto(
                    LocalDateTime.ofInstant(row.visitStart(), doctorZone).toString(),
                    LocalDateTime.ofInstant(row.visitEnd(), doctorZone).toString(),
                    new DoctorSummaryDto(row.doctorFirstName(), row.doctorLastName(), row.doctorTotalPatients())
                ));
            }
        }

        return new PatientListResponse(List.copyOf(patientsById.values()), totalCount);
    }
}