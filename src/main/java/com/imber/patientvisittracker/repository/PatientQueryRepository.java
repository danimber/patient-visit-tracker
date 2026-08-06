package com.imber.patientvisittracker.repository;

import com.imber.patientvisittracker.repository.projection.PatientQueryRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class PatientQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PatientQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PatientQueryRow> findPatients(String search, List<Long> doctorIds, int page, int size) {
        StringBuilder sql = new StringBuilder("""
                WITH filtered_patients AS (
                    SELECT p.id, p.first_name, p.last_name, COUNT(*) OVER() AS total_count
                    FROM patient p
                    WHERE 1 = 1
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("limit", size)
            .addValue("offset", (long) page * size);

        boolean hasDoctorFilter = !CollectionUtils.isEmpty(doctorIds);

        if (StringUtils.hasText(search)) {
            sql.append("      AND LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))\n");
            params.addValue("search", search);
        }

        if (hasDoctorFilter) {
            sql.append("""
                          AND EXISTS (
                            SELECT 1 FROM visit v WHERE v.patient_id = p.id AND v.doctor_id IN (:doctorIds)
                        )
                    """);
            params.addValue("doctorIds", doctorIds);
        }

        sql.append("""
                    ORDER BY p.id
                    LIMIT :limit OFFSET :offset
                ),
                doctor_totals AS (
                    SELECT doctor_id, COUNT(DISTINCT patient_id) AS total_patients
                    FROM visit
                    GROUP BY doctor_id
                ),
                ranked_visits AS (
                    SELECT v.patient_id, v.doctor_id, v.start_date_time, v.end_date_time,
                           d.first_name AS doctor_first_name,
                           d.last_name  AS doctor_last_name,
                           d.timezone   AS doctor_timezone,
                           ROW_NUMBER() OVER (
                               PARTITION BY v.patient_id, v.doctor_id
                               ORDER BY v.start_date_time DESC
                           ) AS rn
                    FROM visit v
                    JOIN doctor d ON d.id = v.doctor_id
                    WHERE v.patient_id IN (SELECT id FROM filtered_patients)
                """);

        if (hasDoctorFilter) {
            sql.append("      AND v.doctor_id IN (:doctorIds)\n");
        }

        sql.append("""
                )
                SELECT
                    fp.id          AS patient_id,
                    fp.first_name  AS patient_first_name,
                    fp.last_name   AS patient_last_name,
                    fp.total_count AS total_count,
                    rv.doctor_id,
                    rv.doctor_first_name,
                    rv.doctor_last_name,
                    rv.doctor_timezone,
                    dt.total_patients,
                    rv.start_date_time,
                    rv.end_date_time
                FROM filtered_patients fp
                LEFT JOIN ranked_visits rv ON rv.patient_id = fp.id AND rv.rn = 1
                LEFT JOIN doctor_totals dt ON dt.doctor_id = rv.doctor_id
                ORDER BY fp.id, rv.start_date_time DESC
                """);

        return jdbc.query(sql.toString(), params, (rs, rowNum) -> {
            Timestamp start = rs.getTimestamp("start_date_time");
            Timestamp end = rs.getTimestamp("end_date_time");
            long doctorId = rs.getLong("doctor_id");
            boolean hasVisit = !rs.wasNull();

            return new PatientQueryRow(
                rs.getLong("patient_id"),
                rs.getString("patient_first_name"),
                rs.getString("patient_last_name"),
                rs.getLong("total_count"),
                hasVisit ? doctorId : null,
                hasVisit ? rs.getString("doctor_first_name") : null,
                hasVisit ? rs.getString("doctor_last_name") : null,
                hasVisit ? rs.getString("doctor_timezone") : null,
                hasVisit ? rs.getInt("total_patients") : null,
                hasVisit && start != null ? start.toInstant() : null,
                hasVisit && end != null ? end.toInstant() : null
            );
        });
    }
}