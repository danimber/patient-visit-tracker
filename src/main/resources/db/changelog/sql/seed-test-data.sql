SET SESSION cte_max_recursion_depth = 10000;

INSERT INTO doctor (first_name, last_name, timezone)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    CONCAT('Doctor', n),
    CONCAT('Surname', n),
    ELT(1 + (n % 7),
        'Europe/Kyiv', 'Europe/Warsaw', 'Europe/London',
        'America/New_York', 'America/Los_Angeles', 'Asia/Tokyo', 'Australia/Sydney')
FROM seq;

-- 5000 patients.
INSERT INTO patient (first_name, last_name)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 5000
)
SELECT CONCAT('Patient', n), CONCAT('Surname', n)
FROM seq;

-- 100 000 visits: for each doctor (1..100), 1000 sequential, non-overlapping
-- 30-minute slots starting 2026-01-01 08:00, 16 slots per working day.
INSERT INTO visit (start_date_time, end_date_time, patient_id, doctor_id)
WITH RECURSIVE doctors AS (
    SELECT 1 AS d
    UNION ALL
    SELECT d + 1 FROM doctors WHERE d < 100
),
slots AS (
    SELECT 1 AS s
    UNION ALL
    SELECT s + 1 FROM slots WHERE s < 1000
)
SELECT
    TIMESTAMPADD(MINUTE, (s - 1) * 30, TIMESTAMPADD(DAY, (s - 1) DIV 16, '2026-01-01 08:00:00')) AS start_date_time,
    TIMESTAMPADD(MINUTE, s * 30,       TIMESTAMPADD(DAY, (s - 1) DIV 16, '2026-01-01 08:00:00')) AS end_date_time,
    1 + ((d * 31 + s * 17) % 5000) AS patient_id,
    d AS doctor_id
FROM doctors CROSS JOIN slots;
