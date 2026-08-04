package com.imber.patientvisittracker.controller;

import com.imber.patientvisittracker.dto.request.CreateVisitRequest;
import com.imber.patientvisittracker.dto.response.VisitResponse;
import com.imber.patientvisittracker.service.VisitService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    @PostMapping
    public ResponseEntity<VisitResponse> createVisit(@Valid @RequestBody CreateVisitRequest request) {
        VisitResponse response = visitService.createVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
