# czar-bank [![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/Dreadblade-dev/czar-bank/blob/main/LICENSE)

**A virtual banking REST api (currently in development)**

**Build status**

master:
![Build status](https://github.com/dreadblade-dev/czar-bank/actions/workflows/workflow.yml/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/Dreadblade-dev/czar-bank/branch/master/graph/badge.svg?token=AW8IRQMF0T)](https://codecov.io/gh/Dreadblade-dev/czar-bank)

development:
![Build status](https://github.com/dreadblade-dev/czar-bank/actions/workflows/workflow.yml/badge.svg)
[![codecov](https://codecov.io/gh/Dreadblade-dev/czar-bank/branch/development/graph/badge.svg?token=AW8IRQMF0T)](https://codecov.io/gh/Dreadblade-dev/czar-bank)

# How to run

## Run postgres

Provide a PostgreSQL instance:

- on `localhost:5432`
- with credentials `postgres:password`
- with database schema `czar_bank`

Or you could run PostgreSQL using docker:

```
docker run -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=czar_bank -p 5432:5432 postgres:14
```

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
