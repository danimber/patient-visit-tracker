package com.imber.patientvisittracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "visit")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date_time", nullable = false)
    private Instant startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private Instant endDateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    protected Visit() {
        // required by JPA
    }

    public Visit(Instant startDateTime, Instant endDateTime, Patient patient, Doctor doctor) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.patient = patient;
        this.doctor = doctor;
    }

}
