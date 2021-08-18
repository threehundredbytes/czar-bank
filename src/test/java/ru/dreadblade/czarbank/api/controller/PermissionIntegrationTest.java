package ru.dreadblade.czarbank.api.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@Disabled
@SpringBootTest
@DisplayName("Permission Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PermissionIntegrationTest extends BaseIntegrationTest  {
}
