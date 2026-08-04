package com.imber.patientvisittracker.repository;

import com.imber.patientvisittracker.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
