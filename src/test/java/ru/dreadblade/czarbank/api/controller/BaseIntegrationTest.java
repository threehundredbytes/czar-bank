package ru.dreadblade.czarbank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = BaseIntegrationTest.DockerPostgreDataSourceInitializer.class)
@Testcontainers
@SpringBootTest
public abstract class BaseIntegrationTest {
    /**
     * Base entity ID values according by .SQL scripts (from test resource package)
     * Usage:
     * To get N entity from the repository: <code>repository.findById(BASE_<ENTITY_NAME>_ID + N)</code>
     * Example:
     * To get 2-nd transaction from repository: <code>transactionRepository(BASE_TRANSACTION_ID + 2L)</code>
     */
    protected final long BASE_PERMISSION_ID = 0L;
    protected final long BASE_ROLE_ID = 12L;
    protected final long BASE_USER_ID = 15L;
    protected final long BASE_BANK_ACCOUNT_TYPE_ID = 19L;
    protected final long BASE_BANK_ACCOUNT_ID = 24L;
    protected final long BASE_TRANSACTION_ID = 29L;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void contextLoads() {
        assertThat(webApplicationContext).isNotNull();
        assertThat(mockMvc).isNotNull();
        assertThat(objectMapper).isNotNull();
    }

    public static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("czar_bank_test");

    static {
        postgresqlContainer.start();
    }

    public static class DockerPostgreDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgresqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresqlContainer.getUsername(),
                    "spring.datasource.password=" + postgresqlContainer.getPassword()
            );
        }
    }
}
