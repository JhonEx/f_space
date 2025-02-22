# Medication Compliance Tracking System

## Overview

The **Medication Compliance Tracking System** is a Java Spring Boot application designed to help healthcare providers and patients manage medication schedules and monitor compliance effectively. The system tracks medication intakes, calculates compliance rates, and generates analytics for better insights into patient adherence.

This project includes:
- **Medication and Intake Management:** Allows users to schedule, record, and track medication intake.
- **Compliance Analytics:** Calculates compliance rates based on recorded intakes.
- **Moving Averages:** Generates simple, exponential, and weighted moving averages for medication intake trends.
- **Skip Reason Tracking:** Logs reasons for skipped medication intakes.
- **Timezone Handling:** Supports intake records across different time zones.

---

## Features

### ✅ Medication and Intake Tracking
- Add, update, and manage medications.
- Schedule medication intake times and days.
- Record intakes with statuses (`TAKEN`, `MISSED`, `SKIPPED`).

### 📊 Compliance Analytics
- Calculate compliance rates for users.
- Generate reports on taken, missed, and skipped doses.
- Identify compliance patterns.

### 📈 Moving Averages Analytics
- Calculate Simple Moving Average (SMA), Exponential Moving Average (EMA), and Weighted Moving Average (WMA) for medication trends.

### 🕒 Timezone Support
- Handles intake timestamps accurately across different time zones.

### ❌ Skip Reasons
- Log reasons for skipped intakes.
- Authorized skipping reasons for better tracking and validation.

---

## Technologies Used

- **Java 17**
- **Spring Boot 3.4.3**
- **Spring Data JPA**
- **H2 In-Memory Database**
- **Postgres Database**
- **Hibernate ORM**
- **JUnit 5 & Mockito** (for testing)
- **Lombok** (for reducing boilerplate code)
- **Jackson** (for JSON processing)

---

## Getting Started

### 📦 Prerequisites

- Java 17+
- Maven 3.8+
- IDE (IntelliJ IDEA recommended)

### 🔧 Setup Instructions

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/JhonEx/f_space.git
1. **Database config**
- H2 in memory database is set by default. To enable Postgres, go to properties file and uncomment lines for postgres.
- Resources folder contain script to improve indexing for postgres db.
   ```bash
   git clone https://github.com/JhonEx/f_space.git

## API Endpoints
The application exposes the following endpoints (accessible at `http://localhost:8080`):

### Analytics Endpoints
1. **Get Moving Averages (SMA)**
    - `GET /api/v1/analytics/moving-averages?userId=1&medicationId=1&type=SMA&days=7`
    - Description: Retrieves the Simple Moving Average for medication intake counts.

2. **Get Moving Averages (WMA)**
    - `GET /api/v1/analytics/moving-averages?userId=1&medicationId=1&type=WMA&days=7`
    - Description: Retrieves the Weighted Moving Average for medication intake counts.

3. **Get Moving Averages (EMA)**
    - `GET /api/v1/analytics/moving-averages?userId=1&medicationId=1&type=EMA&days=7`
    - Description: Retrieves the Exponential Moving Average for medication intake counts.

### Intake Endpoints
4. **Get Intakes by User**
    - `GET /api/v1/intakes/user/1`
    - Description: Retrieves a list of intakes for a specific user.

For detailed responses, parameters, and error handling, refer to the [API Endpoints Documentation](#) or use Swagger UI at `http://localhost:8080/swagger-ui.html`.

### 📦 API Docs

- Access swagger in following URL http://localhost:8080/swagger-ui.html