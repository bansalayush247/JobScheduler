# JobScheduler

JobScheduler is a Spring Boot backend system that schedules and executes webhook jobs based on cron expressions.

## Features

* JWT Authentication
* Custom RBAC Authorization
* Cron-based Job Scheduling
* Webhook Execution
* Retry Mechanism
* Distributed Locking using ShedLock
* Request Logging
* Global Exception Handling

## Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* MySQL
* Maven
* ShedLock
* JWT

## Running the Project Locally

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/jobscheduler.git
cd jobscheduler
```

### 2. Configure Database

Update `application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/jobscheduler
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 3. Install dependencies

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Server will start on:

```
http://localhost:8080
```

## Authentication APIs

Register

```
POST /api/auth/register
```

Login

```
POST /api/auth/login
```

## Job APIs

Create Job

```
POST /api/jobs
```

List Jobs

```
GET /api/jobs
```

Update Job

```
PUT /api/jobs/{id}
```

Update Cron and Payload

```
PUT /api/jobs/{id}
```

Pause Job

```
POST /api/jobs/{id}/pause
```

Resume Job

```
POST /api/jobs/{id}/resume
```

## Admin APIs

Update user role

```
PUT /api/admin/users/{id}/role
```
