package com.imber.patientvisittracker.repository;

import com.imber.patientvisittracker.repository.projection.LastVisitRow;
import com.imber.patientvisittracker.repository.projection.PatientPageResult;
import com.imber.patientvisittracker.repository.projection.PatientRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PatientQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PatientQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PatientPageResult findPatientPage(String search, List<Long> doctorIds, int page, int size) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id, p.first_name, p.last_name, COUNT(*) OVER() AS total_count
                FROM patient p
                WHERE 1 = 1
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", size)
                .addValue("offset", (long) page * size);

        if (StringUtils.hasText(search)) {
            sql.append(" AND LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))\n");
            params.addValue("search", search);
        }

        if (!CollectionUtils.isEmpty(doctorIds)) {
            sql.append("""
                     AND EXISTS (
                        SELECT 1 FROM visit v WHERE v.patient_id = p.id AND v.doctor_id IN (:doctorIds)
                    )
                    """);
            params.addValue("doctorIds", doctorIds);
        }

        sql.append(" ORDER BY p.id LIMIT :limit OFFSET :offset");

        List<PatientRow> rows = new ArrayList<>();
        AtomicLong total = new AtomicLong(0);

        jdbc.query(sql.toString(), params, rs -> {
            rows.add(new PatientRow(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")));
            total.set(rs.getLong("total_count"));
        });

        return new PatientPageResult(rows, total.get());
    }

    public List<LastVisitRow> findLastVisits(List<Long> patientIds, List<Long> doctorIds) {
        if (patientIds.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("""
                SELECT patient_id, doctor_id, start_date_time, end_date_time,
                       doctor_first_name, doctor_last_name, doctor_timezone
                FROM (
                    SELECT v.patient_id       AS patient_id,
                           v.doctor_id        AS doctor_id,
                           v.start_date_time  AS start_date_time,
                           v.end_date_time    AS end_date_time,
                           d.first_name       AS doctor_first_name,
                           d.last_name        AS doctor_last_name,
                           d.timezone         AS doctor_timezone,
                           ROW_NUMBER() OVER (
                               PARTITION BY v.patient_id, v.doctor_id
                               ORDER BY v.start_date_time DESC
                           ) AS rn
                    FROM visit v
                    JOIN doctor d ON d.id = v.doctor_id
                    WHERE v.patient_id IN (:patientIds)
                """);

        MapSqlParameterSource params = new MapSqlParameterSource("patientIds", patientIds);

        if (!CollectionUtils.isEmpty(doctorIds)) {
            sql.append("      AND v.doctor_id IN (:doctorIds)\n");
            params.addValue("doctorIds", doctorIds);
        }

        sql.append("""
                ) ranked
                WHERE rn = 1
                ORDER BY patient_id, start_date_time DESC
                """);

        return jdbc.query(sql.toString(), params, (rs, rowNum) -> new LastVisitRow(
                rs.getLong("patient_id"),
                rs.getLong("doctor_id"),
                rs.getString("doctor_first_name"),
                rs.getString("doctor_last_name"),
                rs.getString("doctor_timezone"),
                rs.getTimestamp("start_date_time").toInstant(),
                rs.getTimestamp("end_date_time").toInstant()
        ));
    }

    public Map<Long, Integer> findTotalPatientsByDoctor(Set<Long> doctorIds) {
        if (doctorIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
            SELECT doctor_id, COUNT(DISTINCT patient_id) AS total_patients
            FROM visit
            WHERE doctor_id IN (:doctorIds)
            GROUP BY doctor_id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("doctorIds", doctorIds);

        Map<Long, Integer> result = new HashMap<>();
        jdbc.query(sql, params, rs -> {
            result.put(rs.getLong("doctor_id"), rs.getInt("total_patients"));
        });
        return result;
    }
}
