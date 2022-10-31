# czar-bank [![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/Dreadblade-dev/czar-bank/blob/main/LICENSE)

A virtual banking REST API (currently in development)

![Build status](https://github.com/dreadblade-dev/czar-bank/actions/workflows/workflow.yml/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/Dreadblade-dev/czar-bank/branch/master/graph/badge.svg?token=AW8IRQMF0T)](https://codecov.io/gh/Dreadblade-dev/czar-bank)

This is Spring Boot demo project. Here you can find examples of:
- Spring Boot (web, components, exception handling, scheduling and properties)
- Spring Security (custom access- and refresh- tokens, 2FA)
- Spring Data JPA (Entities, repositories with custom @Query, composite keys)
- Spring Validation (Inbound and outbound DTO validation)
- Spring Mail
- Flyway (Database migrations)
- Apache Freemarker (For email template)
- Custom TOTP & Recovery codes 
- Integration testing (JUnit & Testcontainers)
- CI/CD (GitHub Actions & CodeCov)
- MapStruct & Lombok

# Features:

- Currency exchange rate API (loaded from the Central Bank of the Russian Federation)
- Bank account & transactions (currently - just a transfer from one account to another, currency exchange supported)
- Authentication based on access tokens that can be refreshed (with refresh-sessions limits per user)
- Two-factor authentication (with totp (via 2fa apps) and x16 one-time recovery codes in `xxxx-xxxx-xxxx-xxxx` format)
- Flexible permissions system (each role contains specific permissions)
- Incoming requests validation

# How to run

## Run postgres

Provide a PostgreSQL instance:

- on `localhost:5432`
- with credentials `postgres:password`
- with database schema `czar_bank`

Or you could run PostgreSQL using docker:

    docker compose up

Or you could provide environment with details about your PostgreSQL instance:

- `POSTGRESQL_HOST` (`localhost` by default)
- `POSTGRESQL_PORT` (`5432` by default)
- `POSTGRESQL_DATABASE` (`czar_bank` by default)
- `POSTGRESQL_USERNAME` (`postgres` by default)
- `POSTGRESQL_PASSWORD` (`password` by default)

## Run czar-bank

This is a Spring Boot application built using Apache Maven. You can build a jar
file and run it from the command line
(it should work if you have Java 17 or newer)

```
git clone https://github.com/Dreadblade-dev/czar-bank.git
cd czar-bank
mvnw -B clean package -DskipTests
java -jar target/czar-bank-0.0.1-SNAPSHOT.jar
```

or you can run it directly using Spring Boot Maven plugin.

```
mvnw spring-boot:run
```

You can access czar-bank at `http://localhost:8080`

There is currently no frontend, but you can use the app using Postman.

To do this, import `czar-bank.postman_collection.json` into Postman.
The Postman collection contains all requests to each czar-bank API endpoint.

Authentication credentials:
- `admin:password` - administrator with full permissions 
- `employee:password` - employee with specific permissions required to help clients
- `client:password` - as client, no permissions

# Email service configuration

In its default configuration, czar-bank uses no operation email service 
which simply logs every email. 

To use SMTP email service you need to activate Spring profile «smtp»
(environment variable `SPRING_PROFILES_ACTIVE=smtp`) and provide
environment with details about your smtp server:

- `SMTP_SERVER_HOST` (e.g. `smtp.gmail.com`)
- `SMTP_SERVER_PORT` (e.g. `465`)
- `SMTP_SERVER_USERNAME` (e.g. `emailaddress@gmail.com`)
- `SMTP_SERVER_PASSWORD` (e.g. `password`)

