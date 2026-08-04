package com.imber.patientvisittracker.repository;

import com.imber.patientvisittracker.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("""
            select count(v) > 0
            from Visit v
            where v.doctor.id = :doctorId
              and v.startDateTime < :end
              and v.endDateTime > :start
            """)
    boolean existsOverlapping(@Param("doctorId") Long doctorId,
                              @Param("start") Instant start,
                              @Param("end") Instant end);
}
