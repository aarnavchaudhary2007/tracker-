# TrackIt — Job & Internship Application Tracker

A full-stack web application to track job and internship applications.
Built with **Spring Boot + H2 + Swagger** (backend) and **vanilla HTML/CSS/JS** (frontend).

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring Data JPA |
| Database   | H2 (in-memory)                      |
| API Docs   | Swagger UI (springdoc-openapi)      |
| Validation | Jakarta Bean Validation             |
| Frontend   | HTML5, CSS3, Vanilla JS             |
| Build      | Maven                               |

---

## Project Structure

```
trackit/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/trackit/
    │   │   ├── TrackitApplication.java        # Entry point
    │   │   ├── config/
    │   │   │   ├── CorsConfig.java            # CORS filter
    │   │   │   └── SwaggerConfig.java         # OpenAPI setup
    │   │   ├── controller/
    │   │   │   └── ApplicationController.java # REST endpoints
    │   │   ├── dto/
    │   │   │   ├── ApplicationRequest.java    # Request body DTO
    │   │   │   ├── ApplicationResponse.java   # Response DTO
    │   │   │   └── StatsResponse.java         # Stats DTO
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── model/
    │   │   │   ├── Application.java           # JPA Entity
    │   │   │   ├── ApplicationStatus.java     # Enum: APPLIED/INTERVIEW/OFFER/REJECTED
    │   │   │   └── ApplicationType.java       # Enum: INTERNSHIP/JOB
    │   │   ├── repository/
    │   │   │   └── ApplicationRepository.java # JPA queries
    │   │   └── service/
    │   │       └── ApplicationService.java    # Business logic
    │   └── resources/
    │       ├── application.properties         # Config
    │       ├── data.sql                       # Sample seed data
    │       └── static/
    │           └── index.html                 # Frontend (served by Spring Boot)
    └── test/
        └── java/com/trackit/
            └── ApplicationServiceTest.java    # Unit tests
```

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

```bash
# 1. Clone / open the project
cd trackit

# 2. Build
mvn clean install

# 3. Run
mvn spring-boot:run
```

### Access

| URL | Description |
|-----|-------------|
| http://localhost:8080 | Frontend (TrackIt UI) |
| http://localhost:8080/api/applications | REST API |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/h2-console | H2 Database Console |

**H2 Console settings:**
- JDBC URL: `jdbc:h2:mem:trackitdb`
- Username: `sa`
- Password: *(leave blank)*

---

## REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/applications` | Get all applications |
| GET | `/api/applications?status=APPLIED` | Filter by status |
| GET | `/api/applications?type=INTERNSHIP` | Filter by type |
| GET | `/api/applications?search=razorpay` | Search by role/company |
| GET | `/api/applications/{id}` | Get one by ID |
| POST | `/api/applications` | Create new application |
| PUT | `/api/applications/{id}` | Full update |
| PATCH | `/api/applications/{id}/status` | Update status only |
| DELETE | `/api/applications/{id}` | Delete |
| GET | `/api/applications/stats` | Get dashboard stats |

---

## Example API Request

### Create Application (POST)
```json
{
  "role": "Backend Developer Intern",
  "company": "Razorpay",
  "type": "INTERNSHIP",
  "status": "APPLIED",
  "dateApplied": "2025-05-23",
  "location": "Bangalore",
  "salaryRange": "25000/month",
  "notes": "Applied via LinkedIn"
}
```

### Update Status (PATCH)
```json
{ "status": "INTERVIEW" }
```

---

## Running Tests

```bash
mvn test
```

---

## Features

- Add, edit, delete applications
- Filter by status (Applied / Interview / Offer / Rejected)
- Filter by type (Internship / Job)
- Search by role or company
- One-click status update from the UI
- Dashboard stats (count by status)
- Sample seed data on startup
- Swagger UI for API testing
- H2 console for database inspection
- CORS enabled for local development
- Bean validation with proper error responses
