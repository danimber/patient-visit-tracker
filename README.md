# Patient Visit Tracker

Patient-to-doctor visit tracking service. Java 21, Spring Boot, MySQL 8.

## Run with Docker

```bash
docker compose up --build
```

That's it — this starts MySQL (auto-seeded with 100 doctors / 5,000 patients / 100,000 visits via Liquibase) and the app on port `8080`.

Stop:
```bash
docker compose down
```

Reset everything (wipes the DB volume, re-seeds from scratch):
```bash
docker compose down -v
docker compose up --build
```

---

## API

### `POST /api/visits`

Create a visit. Rejects overlapping time slots for the same doctor.

**Request body**
```json
{
  "start": "2026-09-01T09:00:00",
  "end": "2026-09-01T09:30:00",
  "patientId": 1,
  "doctorId": 1
}
```
`start` / `end` — ISO-8601 local date-time, in the doctor's own timezone.

**Responses**
| Status | Meaning |
|---|---|
| `201` | Visit created |
| `400` | Invalid payload (bad dates, missing fields, end ≤ start) |
| `404` | Doctor or patient not found |
| `409` | Overlaps an existing visit for that doctor |

---

### `GET /api/patients`

Paginated list of patients with their latest visit to each doctor.

**Query params**
| Param | Default | Description |
|---|---|---|
| `page` | `0` | page number |
| `size` | `20` | page size (max 200) |
| `search` | — | filter by patient name |
| `doctorIds` | — | comma-separated ids, e.g. `1,2,3` |

**Response**
```json
{
  "data": [
    {
      "firstName": "Patient1",
      "lastName": "Surname1",
      "lastVisits": [
        {
          "start": "2026-01-01T09:00:00",
          "end": "2026-01-01T09:30:00",
          "doctor": {
            "firstName": "Doctor31",
            "lastName": "Surname31",
            "totalPatients": 812
          }
        }
      ]
    }
  ],
  "count": 5000
}
```
