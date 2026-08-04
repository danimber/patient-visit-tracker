package com.imber.patientvisittracker.controller;

import com.imber.patientvisittracker.dto.response.PatientListResponse;
import com.imber.patientvisittracker.service.PatientQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientQueryService patientQueryService;

    @GetMapping
    public ResponseEntity<PatientListResponse> listPatients(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String doctorIds
    ) {
        return ResponseEntity.ok(patientQueryService.findPatients(search, parseDoctorIds(doctorIds), page, size));
    }

    private List<Long> parseDoctorIds(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(Long::parseLong)
            .toList();
    }
}

