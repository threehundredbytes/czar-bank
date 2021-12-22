package ru.dreadblade.czarbank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = BaseIntegrationTest.DockerDataSourceInitializer.class)
@Testcontainers
@SpringBootTest
public abstract class BaseIntegrationTest {
    /**
     * <p>Base entity ID values according by .SQL scripts (from test resource package)</p>
     * <p>Usage:</p>
     * <p>To get N entity from the repository: <code>repository.findById(BASE_%ENTITY_NAME%_ID + N)</code></p>
     * <p>Example:</p>
     * <p>To get 2-nd transaction from repository: <code>transactionRepository(BASE_TRANSACTION_ID + 2L)</code></p>
     */
    protected final long BASE_PERMISSION_ID = 0L;
    protected final long BASE_ROLE_ID = 21L;
    protected final long BASE_USER_ID = 24L;
    protected final long BASE_BANK_ACCOUNT_TYPE_ID = 29L;
    protected final long BASE_CURRENCY_ID = 34L;
    protected final long BASE_EXCHANGE_RATE_ID = 38L;
    protected final long BASE_BANK_ACCOUNT_ID = 53L;
    protected final long BASE_TRANSACTION_ID = 58L;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void contextLoads() {
        assertThat(webApplicationContext).isNotNull();
        assertThat(mockMvc).isNotNull();
        assertThat(objectMapper).isNotNull();
    }

    public static Stream<Arguments> getStreamAllUsers() {
        return Stream.of(Arguments.of("admin", "password"),
                Arguments.of("employee", "password"),
                Arguments.of("client", "password"));
    }

    public static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:13"))
            .withCreateContainerCmdModifier(cmd -> cmd.withName("czar-bank-test-postgresql"))
            .withDatabaseName("czar_bank_test");

    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.6"))
            .withCreateContainerCmdModifier(cmd -> cmd.withName("czar-bank-test-redis"))
            .withExposedPorts(6379);

    static {
        postgresqlContainer.start();
        redisContainer.start();
    }

    public static class DockerDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgresqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresqlContainer.getUsername(),
                    "spring.datasource.password=" + postgresqlContainer.getPassword(),
                    "czar-bank.redis.host-name=" + redisContainer.getHost(),
                    "czar-bank.redis.port=" + redisContainer.getFirstMappedPort()
            );
        }
    }
}
